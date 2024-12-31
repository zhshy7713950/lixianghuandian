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
import com.ruimeng.things.UserInfoLiveData
import com.ruimeng.things.home.bean.DeviceDetailBean
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {

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