package com.ruimeng.things

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.GetAdInfoLocal
import com.entity.local.GetCityInfoLocal
import com.entity.local.GetMapKeyLocal
import com.entity.local.GetCustomerServicePhonesLocal
import com.entity.remote.CustomerServiceContactRemote
import com.ruimeng.things.utils.CustomerServiceManager
import com.entity.remote.AdInfoRemote
import com.net.call.BizService
import com.net.whenSuccess
import com.ruimeng.things.home.bean.BannerData
import com.ruimeng.things.home.bean.BannerInfo
import com.ruimeng.things.home.bean.LuckyWheelLotteryBean
import com.ruimeng.things.net_station.LocationUtil
import com.utils.MapUtils
import kotlinx.coroutines.launch
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.utilcode.util.SPUtils
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel : BaseViewModel() {

    private val _homeBannerData = MutableLiveData<List<BannerInfo>>()
    val homeBannerData: LiveData<List<BannerInfo>> get() = _homeBannerData

    private val _meBannerData = MutableLiveData<List<BannerInfo>>()
    val meBannerData: LiveData<List<BannerInfo>> get() = _meBannerData

    // 区域客服电话（城市客服电话）
    private val _customerServicePhones = MutableLiveData<List<CustomerServiceContactRemote>>()
    val customerServicePhones: LiveData<List<CustomerServiceContactRemote>> get() = _customerServicePhones

    private val _luckyWheelLottery = MutableLiveData<LuckyWheelLotteryBean>()
    val luckyWheelLottery: LiveData<LuckyWheelLotteryBean> get() = _luckyWheelLottery

    fun fetchBannerData(context: Context, userId: String, position: String) {
        viewModelScope.launch {
            val mapLocation = requestLocation(context)
            http {
                url = "/apiv6/advertisementinfo/getbanner"
                params["userId"] = userId
                params["position"] = position // 首页
                params["lat"] = mapLocation.latitude.toString()
                params["lng"] = mapLocation.longitude.toString()

                onSuccess { res ->
                    val bannerList = res.toPOJO<BannerData>().data
                    if ("1" == position) {
                        _homeBannerData.value = bannerList
                    } else {
                        _meBannerData.value = bannerList
                    }
                }
            }
        }
    }

    fun fetchLuckyWheelLottery(onResult: ((LuckyWheelLotteryBean) -> Unit)? = null) {
        viewModelScope.launch {
            http {
                IS_SHOW_MSG = false
                url = "/apiv6/luckywheel/getlottery"
                onSuccess { res ->
                    try {
                        val bean = res.toPOJO<LuckyWheelLotteryBean>()
                        _luckyWheelLottery.value = bean
                        onResult?.invoke(bean)
                    } catch (_: Exception) {
                    }
                }
                onFailWithData { code, msg, res ->
                    val bean = res.toPOJO<LuckyWheelLotteryBean>()
                    _luckyWheelLottery.value = bean
                    onResult?.invoke(bean)
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

    /**
     * 获取该用户所在城市的【区域客服】电话
     * 在APP启动时调用，入参为 userId，返回数组 [电话A，电话B，xxx]
     */
    fun fetchCustomerServicePhones(userId: String) {
        viewModelScope.launch {
            BizService.getCustomerServicePhones(GetCustomerServicePhonesLocal(userId))
                .whenSuccess { res ->
                    val contacts = res.data
                    _customerServicePhones.value = contacts
                    CustomerServiceManager.setPhones(contacts)
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
