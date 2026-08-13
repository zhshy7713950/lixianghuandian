package com.ruimeng.things.net_station

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.ruimeng.things.App
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

enum class LocationSource {
    LIVE,
    MEMORY_CACHE,
    DEFAULT_ZERO
}

enum class LocationFailure {
    PERMISSION_DENIED,
    PROVIDER_DISABLED,
    MANAGER_UNAVAILABLE,
    SECURITY_EXCEPTION,
    TIMEOUT
}

data class LocationResult(
    val coordinates: Coordinates,
    val source: LocationSource,
    val failure: LocationFailure? = null
)

/**
 * 线程安全的一次性完成门闩。定位成功、失败和协程取消只能有一个分支获胜，
 * 从而避免 GPS 与网络定位同时回调时重复恢复同一个协程。
 */
internal class SingleShotCompletion(private val cleanup: () -> Unit) {
    private val completed = AtomicBoolean(false)

    val isCompleted: Boolean
        get() = completed.get()

    fun tryComplete(action: () -> Unit): Boolean {
        if (!completed.compareAndSet(false, true)) return false
        cleanup()
        action()
        return true
    }

    fun cancel() {
        tryComplete { }
    }
}

/** 当前进程内的最后一次有效坐标缓存，不写入磁盘，避免下次启动复用过期位置。 */
internal class LocationMemoryCache {
    @Volatile
    private var lastSuccessfulCoordinates: Coordinates? = null

    fun live(coordinates: Coordinates): LocationResult {
        lastSuccessfulCoordinates = coordinates
        return LocationResult(coordinates, LocationSource.LIVE)
    }

    fun fallback(failure: LocationFailure): LocationResult {
        val cached = lastSuccessfulCoordinates
        return if (cached != null) {
            LocationResult(cached, LocationSource.MEMORY_CACHE, failure)
        } else {
            LocationResult(Coordinates(0.0, 0.0), LocationSource.DEFAULT_ZERO, failure)
        }
    }
}

object LocationUtil {
    private const val TAG = "LocationUtil"
    private const val DEFAULT_TIMEOUT_MILLIS = 10_000L
    private const val MIN_TIME = 1000L
    private const val MIN_DISTANCE = 1f

    private val memoryCache = LocationMemoryCache()

    /**
     * 获取一次位置。GPS 和网络定位可以同时请求，但只接受第一个结果。
     * 定位失败时依次降级为本进程缓存坐标和 0,0，调用方不会被永久挂起。
     */
    suspend fun resolveLocation(
        context: Context,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
    ): LocationResult {
        require(timeoutMillis > 0) { "timeoutMillis must be greater than 0" }

        val liveResult = withTimeoutOrNull(timeoutMillis) {
            awaitLiveLocation(context.applicationContext)
        } ?: LiveLocationResult.Failure(LocationFailure.TIMEOUT)

        return when (liveResult) {
            is LiveLocationResult.Success -> {
                val result = memoryCache.live(liveResult.coordinates)
                // 兼容项目中仍直接读取 App 经纬度的旧业务。
                App.lat = result.coordinates.latitude
                App.lng = result.coordinates.longitude
                result
            }
            is LiveLocationResult.Failure -> {
                memoryCache.fallback(liveResult.reason).also { result ->
                    Log.w(
                        TAG,
                        "Live location failed: ${liveResult.reason}; fallback=${result.source}"
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitLiveLocation(context: Context): LiveLocationResult =
        suspendCancellableCoroutine { continuation ->
            var locationManager: LocationManager? = null
            var locationListener: LocationListener? = null

            fun stopLocationUpdates() {
                val manager = locationManager ?: return
                val listener = locationListener ?: return
                try {
                    manager.removeUpdates(listener)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location updates", e)
                }
            }

            val completion = SingleShotCompletion(::stopLocationUpdates)

            fun complete(result: LiveLocationResult) {
                completion.tryComplete {
                    continuation.resume(result)
                }
            }

            continuation.invokeOnCancellation {
                completion.cancel()
            }

            val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (manager == null) {
                complete(LiveLocationResult.Failure(LocationFailure.MANAGER_UNAVAILABLE))
                return@suspendCancellableCoroutine
            }
            locationManager = manager

            val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasFineLocationPermission && !hasCoarseLocationPermission) {
                complete(LiveLocationResult.Failure(LocationFailure.PERMISSION_DENIED))
                return@suspendCancellableCoroutine
            }

            try {
                // GPS_PROVIDER 需要精确定位权限；仅授予大致位置时仍可使用网络定位。
                val canUseGps = hasFineLocationPermission &&
                    manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val canUseNetwork = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (!canUseGps && !canUseNetwork) {
                    complete(LiveLocationResult.Failure(LocationFailure.PROVIDER_DISABLED))
                    return@suspendCancellableCoroutine
                }

                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        complete(
                            LiveLocationResult.Success(
                                Coordinates(location.latitude, location.longitude)
                            )
                        )
                    }

                    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                    override fun onProviderEnabled(provider: String) {}

                    override fun onProviderDisabled(provider: String) {}
                }
                locationListener = listener

                fun requestUpdates(provider: String) {
                    if (completion.isCompleted) return
                    manager.requestLocationUpdates(
                        provider,
                        MIN_TIME,
                        MIN_DISTANCE,
                        listener,
                        Looper.getMainLooper()
                    )
                    // 处理“取消发生在完成检查与注册之间”的竞争，避免监听残留。
                    if (completion.isCompleted) {
                        stopLocationUpdates()
                    }
                }

                if (canUseNetwork) {
                    requestUpdates(LocationManager.NETWORK_PROVIDER)
                }
                if (canUseGps) {
                    requestUpdates(LocationManager.GPS_PROVIDER)
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Location request failed with SecurityException", e)
                complete(LiveLocationResult.Failure(LocationFailure.SECURITY_EXCEPTION))
            } catch (e: RuntimeException) {
                Log.w(TAG, "LocationManager is unavailable", e)
                complete(LiveLocationResult.Failure(LocationFailure.MANAGER_UNAVAILABLE))
            }
        }

    private sealed class LiveLocationResult {
        data class Success(val coordinates: Coordinates) : LiveLocationResult()
        data class Failure(val reason: LocationFailure) : LiveLocationResult()
    }
}
