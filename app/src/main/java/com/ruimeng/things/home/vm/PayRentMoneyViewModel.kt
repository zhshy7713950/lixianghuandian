package com.ruimeng.things.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.AgentByCodeLocal
import com.entity.remote.AgentInfoRemote
import com.net.call.BizService
import com.net.whenSuccess
import kotlinx.coroutines.launch

class PayRentMoneyViewModel: BaseViewModel() {

    private val _agentInf: MutableLiveData<AgentInfoRemote> = MutableLiveData()
    val agentInfo: LiveData<AgentInfoRemote> = _agentInf

    fun getAgentByCode(deviceId: String){
        viewModelScope.launch {
            BizService.getAgentByCode(AgentByCodeLocal(deviceId)).whenSuccess {
                _agentInf.value = it.data
            }
        }
    }

}