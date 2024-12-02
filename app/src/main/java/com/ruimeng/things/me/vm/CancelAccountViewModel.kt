package com.ruimeng.things.me.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.GetCodeLocal
import com.entity.local.UnregisterLocal
import com.net.call.BizService
import com.net.whenSuccess
import kotlinx.coroutines.launch

class CancelAccountViewModel : BaseViewModel() {
    private val _getCodeLiveData: MutableLiveData<String> = MutableLiveData()
    val getCodeLiveData = _getCodeLiveData

    private val _unregisterLiveData: MutableLiveData<String> = MutableLiveData()
    val unregisterLiveData = _unregisterLiveData

    fun getCode(mobile: String){
        viewModelScope.launch{
            BizService.getCode(GetCodeLocal(mobile)).whenSuccess {
                getCodeLiveData.value = it.errmsg
            }
        }
    }

    fun unregister(userId: String,mobile: String, code: String){
        viewModelScope.launch{
            BizService.unregister(UnregisterLocal(userId,mobile,code)).whenSuccess {
                unregisterLiveData.value = it.errmsg
            }
        }
    }
}