package com.ruimeng.things

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.OneKeyLoginLocal
import com.entity.remote.LoginRemote
import com.entity.remote.ResCommon
import com.net.NetworkResponse
import com.net.call.BizService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : BaseViewModel() {

    fun oneKeyLogin(oneKeyLoginLocal: OneKeyLoginLocal): LiveData<NetworkResponse<ResCommon<LoginRemote>>> {
        val loginLiveData = MutableLiveData<NetworkResponse<ResCommon<LoginRemote>>>()
        viewModelScope.launch {
            val response = BizService.oneKeyLogin(oneKeyLoginLocal,false)
            loginLiveData.value = response
        }
        return loginLiveData
    }

}