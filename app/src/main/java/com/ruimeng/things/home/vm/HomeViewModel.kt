package com.ruimeng.things.home.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.ChangeErrorLocal
import com.entity.local.GetNewAppVerLocal
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
import com.ruimeng.things.ads.AdManager
import com.ruimeng.things.home.bean.DeviceDetailBean
import com.ruimeng.things.home.bean.MyDevicesBean
import com.ruimeng.things.voice.VoicePlayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wongxd.Config

class HomeViewModel : BaseViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _adStatusLiveData = MutableLiveData<Boolean>()
    val adStatusLiveData: LiveData<Boolean> = _adStatusLiveData

    /**
     * 静默检查广告开关状态 + 应用商店审核状态
     *
     * 在APP启动时调用：
     * 1. 请求 getnewappver，若 APP 版本号 > 平台版本号 → 审核中 → 隐藏广告
     * 2. 请求 getthridadstatus，结合审核状态决定是否开启广告
     */
    fun checkAdStatusSilently(): LiveData<Boolean> {
        viewModelScope.launch {
            // 1）审核中判断：APP版本 > 平台版本 → 审核中
            val underReview = resolveUnderReviewStatus()
            AdManager.getInstance().setUnderReview(underReview)

            // 2）广告总开关
            val response = BizService.getThirdAdStatus()
            response.whenSuccess { data ->
                val switchOn = data.data.switch == 1
                val isAdEnabled = switchOn && !underReview
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

    /**
     * 请求后管平台版本号，并与本地 APP 版本比较
     * @return true 表示审核中（应隐藏广告）
     */
    private suspend fun resolveUnderReviewStatus(): Boolean {
        return try {
            when (val response = BizService.getNewAppVer(GetNewAppVerLocal())) {
                is NetworkResponse.Success -> {
                    val platformVer = response.data.data.updateVer.orEmpty()
                    val appVer = Config.getDefault().versionName
                    if (platformVer.isBlank() || appVer.isBlank()) {
                        false
                    } else {
                        val underReview = AdManager.getInstance().compareVersion(appVer, platformVer) > 0
                        Log.d(TAG, "版本比较 app=$appVer platform=$platformVer underReview=$underReview")
                        underReview
                    }
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "getnewappver 失败", e)
            false
        }
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

    fun changeError(context: Context,deviceId: String, code: String): LiveData<String> {
        val changeErrorLiveData = MutableLiveData<String>()
        viewModelScope.launch {
            BizService.changeError(ChangeErrorLocal(deviceId, code)).whenSuccess {
                // 播放成功语音
                VoicePlayerManager.getInstance().playVoice(context, "success-8")
                changeErrorLiveData.value = it.errmsg
            }.whenError { _, msg ->
                // 播放失败语音
                VoicePlayerManager.getInstance().playVoice(context, "fail-1")
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