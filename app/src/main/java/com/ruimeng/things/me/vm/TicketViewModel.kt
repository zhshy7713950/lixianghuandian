package com.ruimeng.things.me.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.UserPaymentInfoLocal
import com.entity.remote.UserPaymentInfoRemote
import com.net.call.BizService
import com.net.whenSuccess
import kotlinx.coroutines.launch

class TicketViewModel : BaseViewModel() {

    private val _userPaymentInfoLiveData: MutableLiveData<UserPaymentInfoRemote> = MutableLiveData()
    val userPaymentInfo = _userPaymentInfoLiveData

    fun getUserPaymentInfo(userId: String, deviceId: String) {
        viewModelScope.launch {
            BizService.getUserPaymentInfo(UserPaymentInfoLocal(userId, deviceId)).whenSuccess {
                _userPaymentInfoLiveData.value = it.data
            }
        }
    }
}