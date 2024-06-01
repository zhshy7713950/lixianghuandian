package com.ruimeng.things.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.RentStep1Local
import com.entity.remote.RentStep1Remote
import com.entity.remote.ResCommon
import com.net.call.BizService
import com.net.getOrElse
import com.net.whenSuccess
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {

    private val _rentStep1LiveData = MutableLiveData<ResCommon<RentStep1Remote>>()
    val rentStep1LiveData: LiveData<ResCommon<RentStep1Remote>> = _rentStep1LiveData

    fun rentStep1(deviceId: String, cgModel: String) {
        viewModelScope.launch {
            BizService.rentStep1(RentStep1Local(deviceId, cgModel)).whenSuccess {
                _rentStep1LiveData.value = it
            }
        }
    }
}