package com.ruimeng.things.me.vm

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.UserPaymentInfoLocal
import com.entity.remote.UserPaymentInfoRemote
import com.net.call.BizService
import com.net.whenSuccess
import com.ruimeng.things.App
import com.ruimeng.things.home.bean.BannerData
import com.ruimeng.things.home.bean.BannerInfo
import com.ruimeng.things.net_station.LocationUtil
import kotlinx.coroutines.launch
import wongxd.common.toPOJO
import wongxd.http
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TicketViewModel : BaseViewModel() {

    private val _userPaymentInfoLiveData: MutableLiveData<UserPaymentInfoRemote> = MutableLiveData()
    val userPaymentInfo = _userPaymentInfoLiveData

    private val _bannerData = MutableLiveData<List<BannerInfo>>()
    val bannerData: LiveData<List<BannerInfo>> get() = _bannerData

    fun getUserPaymentInfo(userId: String, deviceId: String) {
        viewModelScope.launch {
            BizService.getUserPaymentInfo(UserPaymentInfoLocal(userId, deviceId)).whenSuccess {
                _userPaymentInfoLiveData.value = it.data
            }
        }
    }

    fun fetchBannerData(context: Context, userId: String) {
        viewModelScope.launch {
            val mapLocation = requestLocation(context)
            http {
                url = "/apiv6/advertisementinfo/getbanner"
                params["userId"] = userId
                params["position"] = "1" // 优惠券页面
                params["lat"] = mapLocation.latitude.toString()
                params["lng"] = mapLocation.longitude.toString()

                onSuccess { res ->
                    val bannerList = res.toPOJO<BannerData>().data
                    _bannerData.value = bannerList
                }
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
}