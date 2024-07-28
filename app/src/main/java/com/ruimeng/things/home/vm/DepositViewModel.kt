package com.ruimeng.things.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.net.call.BizService
import com.net.whenSuccess
import com.ruimeng.things.home.bean.MyDevicesBean
import kotlinx.coroutines.launch

class DepositViewModel: BaseViewModel() {

    fun getMyDevice(): LiveData<List<MyDevicesBean.Data>>{
        val myDevicesLiveData = MutableLiveData<List<MyDevicesBean.Data>>()
        viewModelScope.launch {
            BizService.getMyDevice().whenSuccess {
                myDevicesLiveData.value = it.data
            }
        }
        return myDevicesLiveData
    }

}