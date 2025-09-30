package com.ruimeng.things.me.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.VoiceOpenLocal
import com.net.call.BizService
import com.net.whenError
import com.net.whenSuccess
import com.ruimeng.things.UserInfoLiveData
import kotlinx.coroutines.launch

/**
 * 设置页面ViewModel
 * 负责处理设置页面相关的业务逻辑
 */
class SettingViewModel : BaseViewModel() {

    private val _voiceSwitchLiveData = MutableLiveData<VoiceSwitchEvent>()
    val voiceSwitchLiveData: LiveData<VoiceSwitchEvent> = _voiceSwitchLiveData

    /**
     * 更新语音开关状态
     * 
     * @param userId 用户ID
     * @param isVoiceActived 语音开关状态，1-启用，0-关闭
     */
    fun updateVoiceSwitch(userId: String, isVoiceActived: Int) {
        viewModelScope.launch {
            try {
                val voiceOpenLocal = VoiceOpenLocal(
                    userId = userId,
                    isVoiceActived = isVoiceActived.toString()
                )
                
                val response = BizService.voiceOpen(voiceOpenLocal)
                response.whenSuccess { data ->
                    _voiceSwitchLiveData.value = VoiceSwitchEvent.Success(
                        isActivated = isVoiceActived == 1,
                        message = if (isVoiceActived == 1) "语音提示已开启" else "语音提示已关闭"
                    )
                }.whenError { _, errorMsg ->
                    _voiceSwitchLiveData.value = VoiceSwitchEvent.Error(
                        message = "操作失败：$errorMsg"
                    )
                }
            } catch (e: Exception) {
                _voiceSwitchLiveData.value = VoiceSwitchEvent.Error(
                    message = "操作失败：${e.message}"
                )
            }
        }
    }

    /**
     * 获取当前语音开关状态
     *
     * @return 当前语音开关状态，true表示开启，false表示关闭
     */
    fun getCurrentVoiceSwitchStatus(): Boolean {
        return UserInfoLiveData.getCurrentVoiceSwitchStatus()
    }
}

/**
 * 语音开关事件
 */
sealed class VoiceSwitchEvent {
    /**
     * 操作成功
     * @param isActivated 是否已激活
     * @param message 提示消息
     */
    data class Success(
        val isActivated: Boolean,
        val message: String
    ) : VoiceSwitchEvent()

    /**
     * 操作失败
     * @param message 错误消息
     */
    data class Error(
        val message: String
    ) : VoiceSwitchEvent()
}
