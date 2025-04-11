package com.ruimeng.things

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amap.api.location.AMapLocation
import com.base.viewmodel.BaseViewModel
import com.entity.local.GetAdInfoLocal
import com.entity.local.GetCityInfoLocal
import com.entity.local.GetMapKeyLocal
import com.entity.remote.AdInfoRemote
import com.net.call.BizService
import com.net.whenSuccess
import com.ruimeng.things.home.bean.BannerData
import com.ruimeng.things.home.bean.BannerInfo
import com.ruimeng.things.net_station.LocationUtil
import com.utils.MapUtils
import com.utils.unsafeLazy
import kotlinx.coroutines.launch
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.utilcode.util.SPUtils
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MainViewModel : BaseViewModel() {

    private val _bannerData = MutableLiveData<List<BannerInfo>>()
    val bannerData: LiveData<List<BannerInfo>> get() = _bannerData

    fun fetchBannerData(context: Context,userId: String) {
        viewModelScope.launch {
            val mapLocation = requestLocation(context)
            http {
                url = "/apiv6/advertisementinfo/getbanner"
                params["userId"] = userId
                params["position"] = "1" // 首页
                params["lat"] = mapLocation.latitude.toString()
                params["lng"] = mapLocation.longitude.toString()

                onSuccess { res ->
                    val bannerList = res.toPOJO<BannerData>().data
                    _bannerData.value = bannerList
                }
            }
        }
    }

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
            BizService.getAdInfo(
                GetAdInfoLocal(
                    userId,
                    mapLocation.latitude.toString(),
                    mapLocation.longitude.toString()
                )
            )
                .whenSuccess {
                    _adInfoLiveData.value = it.data
                }
        }
    }

    fun requestCityInfo(context: Context) {
        viewModelScope.launch {
            val mapLocation = requestLocation(context)
            val cityInfo = BizService.getCityInfo(
                GetCityInfoLocal(
                    mapLocation.latitude.toString(),
                    mapLocation.longitude.toString()
                )
            )
            cityInfo.whenSuccess {
                App.province = it.data.province
                App.city = it.data.city
            }
        }
    }

    private suspend fun requestLocation(context: Context) = suspendCoroutine<Location> { con ->
        LocationUtil.getLocation(context, object : LocationUtil.Companion.LocationCallback {
            override fun onLocationReceived(location: Location) {
                App.lat = location.latitude
                App.lng = location.longitude
                con.resume(location)
            }

            override fun onLocationFailed(errorMessage: String) {
            }

        })
    }

//    private val adInfoLiveData = MutableLiveData<>()

}