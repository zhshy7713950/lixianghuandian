package com.ruimeng.things

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.OneKeyLoginLocal
import com.entity.local.GetCaptchaLocal
import com.entity.local.CheckCaptchaLocal
import com.entity.local.UploadVersionLocal
import com.entity.remote.LoginRemote
import com.entity.remote.ResCommon
import com.net.NetworkResponse
import com.net.call.BizService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wongxd.Config

class LoginViewModel : BaseViewModel() {

    fun oneKeyLogin(oneKeyLoginLocal: OneKeyLoginLocal): LiveData<NetworkResponse<ResCommon<LoginRemote>>> {
        val loginLiveData = MutableLiveData<NetworkResponse<ResCommon<LoginRemote>>>()
        viewModelScope.launch {
            val response = BizService.oneKeyLogin(oneKeyLoginLocal,false)
            loginLiveData.value = response
        }
        return loginLiveData
    }

    fun uploadVersion(){
        GlobalScope.launch {
            BizService.uploadVersion(UploadVersionLocal(
                userId = UserInfoLiveData.getFromString()?.id,
                manufacturer = android.os.Build.MANUFACTURER,
                model = android.os.Build.MODEL,
                system_type = "Android",
                system_version = android.os.Build.VERSION.RELEASE,
                app_version = Config.getDefault().versionName
            ))
        }
    }

    fun getCaptcha(getCaptchaLocal: GetCaptchaLocal): LiveData<NetworkResponse<ResCommon<String>>> {
        val liveData = MutableLiveData<NetworkResponse<ResCommon<String>>>()
        viewModelScope.launch {
            val response = BizService.getCaptcha(getCaptchaLocal)
            liveData.value = response
        }
        return liveData
    }

    fun checkCaptcha(checkCaptchaLocal: CheckCaptchaLocal): LiveData<NetworkResponse<ResCommon<Any>>> {
        val liveData = MutableLiveData<NetworkResponse<ResCommon<Any>>>()
        viewModelScope.launch {
            val response = BizService.checkCaptcha(checkCaptchaLocal)
            liveData.value = response
        }
        return liveData
    }

}