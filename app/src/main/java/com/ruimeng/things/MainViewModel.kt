package com.ruimeng.things

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amap.api.location.AMapLocation
import com.base.viewmodel.BaseViewModel
import com.entity.local.GetAdInfoLocal
import com.entity.local.GetMapKeyLocal
import com.entity.remote.AdInfoRemote
import com.net.call.BizService
import com.net.whenSuccess
import com.ruimeng.things.net_station.AMapLocUtils
import com.utils.MapUtils
import com.utils.unsafeLazy
import kotlinx.coroutines.launch
import wongxd.utils.utilcode.util.SPUtils
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel : BaseViewModel() {
    fun getMapKey() {
        val localMapKey = SPUtils.getInstance().getString("map_key")
        MapUtils.initializer(localMapKey)
        viewModelScope.launch {
            BizService.getAMapKey(GetMapKeyLocal()).whenSuccess {
                it.data?.let { apiKey ->
                    if (apiKey.isNotEmpty()) {
                        SPUtils.getInstance().put("map_key", apiKey)
                        MapUtils.initializer(apiKey)
                    }
                }
            }
        }
    }

    private val _adInfoLiveData = MutableLiveData<AdInfoRemote>()
    val adInfoLiveData: LiveData<AdInfoRemote> = _adInfoLiveData

    fun getAdInfo(context: Context, userId: String) {
        viewModelScope.launch {
            val mapLocation = requestLocation(context)
            if (BuildConfig.DEBUG) {//debug mock
                BizService.getAdInfo(GetAdInfoLocal(userId, "30.57", "104.07"))
            } else {
                BizService.getAdInfo(
                    GetAdInfoLocal(
                        userId,
                        mapLocation.latitude.toString(),
                        mapLocation.longitude.toString()
                    )
                )
            }.whenSuccess {
                _adInfoLiveData.value = it.data
            }
        }
    }

    private val mapLocUtils by unsafeLazy {
        AMapLocUtils()
    }

    private suspend fun requestLocation(context: Context) = suspendCoroutine<AMapLocation> { con ->
        mapLocUtils.getLonLat(context) {
            con.resume(it)
        }
    }

//    private val adInfoLiveData = MutableLiveData<>()

}