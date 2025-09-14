package com.ruimeng.things.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.ChangeErrorLocal
import com.entity.local.OneDeviceLocal
import com.entity.local.RentStep1Local
import com.entity.remote.RentStep1Remote
import com.entity.remote.ResCommon
import com.net.NetworkResponse
import com.net.call.BizService
import com.net.getOrElse
import com.net.isSuccess
import com.net.whenError
import com.net.whenSuccess
import com.ruimeng.things.SplashViewModel
import com.ruimeng.things.UserInfoLiveData
import com.ruimeng.things.ads.AdManager
import com.ruimeng.things.home.bean.DeviceDetailBean
import com.ruimeng.things.home.bean.MyDevicesBean
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {

    private val _adStatusLiveData = MutableLiveData<Boolean>()
    val adStatusLiveData: LiveData<Boolean> = _adStatusLiveData

    /**
     * 静默检查广告开关状态
     *
     * 在APP启动时调用此方法获取广告开关状态，不阻塞UI
     * @return LiveData<Boolean> 广告开关状态，true表示开启，false表示关闭
     */
    fun checkAdStatusSilently(): LiveData<Boolean> {
        viewModelScope.launch {
            val response = BizService.getThirdAdStatus()
            // 处理响应结果
            response.whenSuccess { data ->
                val isAdEnabled = data.data.switch == 1
                AdManager.getInstance().setAdEnabled(isAdEnabled)
                _adStatusLiveData.value = isAdEnabled
            }.whenError { _, _ ->
                // 网络错误时，默认关闭广告
                AdManager.getInstance().setAdEnabled(false)
                _adStatusLiveData.value = false
            }
        }
        return adStatusLiveData
    }

    fun getMyDevice(): LiveData<List<MyDevicesBean.Data>>{
        val myDevicesLiveData = MutableLiveData<List<MyDevicesBean.Data>>()
        viewModelScope.launch {
            BizService.getMyDevice().whenSuccess {
                myDevicesLiveData.value = it.data
            }
        }
        return myDevicesLiveData
    }

    val userInfo: UserInfoLiveData = UserInfoLiveData.getInstance()

    fun rentStep1(deviceId: String, cgModel: String): LiveData<ResCommon<RentStep1Remote>> {
        val rentStep1LiveData = MutableLiveData<ResCommon<RentStep1Remote>>()
        viewModelScope.launch {
            BizService.rentStep1(RentStep1Local(deviceId, cgModel)).whenSuccess {
                rentStep1LiveData.value = it
            }
        }
        return rentStep1LiveData
    }

    fun changeError(deviceId: String, code: String): LiveData<String> {
        val changeErrorLiveData = MutableLiveData<String>()
        viewModelScope.launch {
            BizService.changeError(ChangeErrorLocal(deviceId, code)).whenSuccess {
                changeErrorLiveData.value = it.errmsg
            }.whenError { _, msg ->
                changeErrorLiveData.value = msg
            }
        }
        return changeErrorLiveData
    }

    var isPollingServerDeviceStatus = false
    private val _deviceDetailLiveData = MutableLiveData<GetDeviceStatusEvent>()
    val deviceDetailLiveData: LiveData<GetDeviceStatusEvent> =
        _deviceDetailLiveData

    fun pollDeviceStatus(isOpen: Boolean, deviceId: String) {
        if (isPollingServerDeviceStatus) {
            return
        }
        viewModelScope.launch {
            isPollingServerDeviceStatus = true
            var tayCount = 10
            while (tayCount > 0) {
                if (!isPollingServerDeviceStatus) {
                    break
                }
                val deviceStatus = BizService.getOneDevice(OneDeviceLocal(deviceId))
                if (deviceStatus.isSuccess) {
                    if (isOpen && (deviceStatus as NetworkResponse.Success)?.data?.data?.device_base.device_status == "2" ||
                        !isOpen && (deviceStatus as NetworkResponse.Success)?.data?.data?.device_base.device_status == "1"
                    ) {
                        _deviceDetailLiveData.value = GetDeviceStatusEvent.Success(deviceStatus.data.data)
                        break
                    }
                }
                if (!isPollingServerDeviceStatus) {
                    break
                }
                delay(3000)
            }
            isPollingServerDeviceStatus = false
            if (tayCount <= 0) {
                _deviceDetailLiveData.value = GetDeviceStatusEvent.Error("电池数据同步失败，请手动刷新后重试")
            }
        }
    }
}

sealed class GetDeviceStatusEvent {
    data class Success(val deviceDetail: DeviceDetailBean.Data) : GetDeviceStatusEvent()
    data class Error(val error: String) : GetDeviceStatusEvent()
}