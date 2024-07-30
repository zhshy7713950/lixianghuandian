package com.ruimeng.things.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.ChangeErrorLocal
import com.entity.local.RentStep1Local
import com.entity.remote.RentStep1Remote
import com.entity.remote.ResCommon
import com.net.call.BizService
import com.net.getOrElse
import com.net.whenError
import com.net.whenSuccess
import com.ruimeng.things.UserInfoLiveData
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {

    val userInfo: UserInfoLiveData = UserInfoLiveData.getInstance()

    fun rentStep1(deviceId: String, cgModel: String): LiveData<ResCommon<RentStep1Remote>>{
        val rentStep1LiveData = MutableLiveData<ResCommon<RentStep1Remote>>()
        viewModelScope.launch {
            BizService.rentStep1(RentStep1Local(deviceId, cgModel)).whenSuccess {
                rentStep1LiveData.value = it
            }
        }
        return rentStep1LiveData
    }

    fun changeError(deviceId: String, code: String): LiveData<String>{
        val changeErrorLiveData = MutableLiveData<String>()
        viewModelScope.launch{
            BizService.changeError(ChangeErrorLocal(deviceId, code)).whenSuccess{
                changeErrorLiveData.value = it.errmsg
            }.whenError { _, msg ->
                changeErrorLiveData.value = msg
            }
        }
        return changeErrorLiveData
    }
}