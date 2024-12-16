package com.ruimeng.things.net_station

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat

class LocationUtil {

    companion object {
        private const val MIN_TIME = 1000L // 最小更新时间间隔（单位：毫秒）
        private const val MIN_DISTANCE = 1f // 最小更新距离间隔（单位：米）

        // 定义回调接口
        interface LocationCallback {
            fun onLocationReceived(location: Location)
            fun onLocationFailed(errorMessage: String)
        }

        fun getLocation(context: Context, callback: LocationCallback) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    ?: run {
                        callback.onLocationFailed("LocationManager is null")
                        return
                    }

            // 检查权限
            val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasFineLocationPermission && !hasCoarseLocationPermission) {
                callback.onLocationFailed("Location permissions not granted")
                return
            }

            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                callback.onLocationFailed("GPS and Network providers are not enabled")
                return
            }

            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    stopLocationUpdates(locationManager, this)
                    callback.onLocationReceived(location)
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {}
            }

            try {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME,
                        MIN_DISTANCE,
                        locationListener
                    )
                }
                if (isGpsEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME,
                        MIN_DISTANCE,
                        locationListener
                    )
                }
            } catch (e: SecurityException) {
                callback.onLocationFailed("Security exception: ${e.message}")
            }
        }

        private fun stopLocationUpdates(
            locationManager: LocationManager,
            locationListener: LocationListener
        ) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: SecurityException) {
                // 可以在这里添加日志记录异常情况
            }
        }
    }
}