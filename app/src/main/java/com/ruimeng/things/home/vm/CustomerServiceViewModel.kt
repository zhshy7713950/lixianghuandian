package com.ruimeng.things.home.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.ChangeErrorLocal
import com.entity.local.UserPaymentInfoLocal
import com.entity.remote.ResCommon
import com.entity.remote.UserPaymentInfoRemote
import com.net.NetworkResponse
import com.net.call.BizService
import com.net.whenError
import com.net.whenSuccess
import com.ruimeng.things.voice.VoicePlayerManager
import kotlinx.coroutines.launch

class CustomerServiceViewModel : BaseViewModel() {

    /**
     * 获取用户支付信息（电池状态）
     */
    fun getUserPaymentInfo(userId: String, deviceId: String): LiveData<NetworkResponse<ResCommon<UserPaymentInfoRemote>>> {
        val liveData = MutableLiveData<NetworkResponse<ResCommon<UserPaymentInfoRemote>>>()
        viewModelScope.launch {
            val response = BizService.getUserPaymentInfo(UserPaymentInfoLocal(userId, deviceId))
            liveData.value = response
        }
        return liveData
    }

    /**
     * 自助开仓接口
     */
    fun changeError(context: Context,deviceId: String, code: String): LiveData<String> {
        val changeErrorLiveData = MutableLiveData<String>()
        viewModelScope.launch {
            BizService.changeError(ChangeErrorLocal(deviceId, code)).whenSuccess {
                // 播放成功语音
                VoicePlayerManager.getInstance().playVoice(context, "success-8")
                changeErrorLiveData.value = it.errmsg
            }.whenError { i, s ->
                // 播放失败语音
                VoicePlayerManager.getInstance().playVoice(context, "fail-1")
            }
        }
        return changeErrorLiveData
    }
}
