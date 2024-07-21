package com.ruimeng.things.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.AdPayLocal
import com.entity.local.ServerPayResultLocal
import com.entity.remote.ResCommon
import com.net.NetworkResponse
import com.net.call.BizService
import com.net.getOrNull
import com.net.isSuccess
import com.ruimeng.things.home.bean.GetRentPayBean
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CouponPurchaseViewModel : BaseViewModel() {
    fun adPay(adPayLocal: AdPayLocal): LiveData<NetworkResponse<ResCommon<GetRentPayBean.PayData>>> {
        val adPayLiveData = MutableLiveData<NetworkResponse<ResCommon<GetRentPayBean.PayData>>>()
        viewModelScope.launch {
            adPayLiveData.value = BizService.adPay(adPayLocal)
        }
        return adPayLiveData
    }

    private val _serverPayResultLiveData = MutableLiveData<CouponPurchaseEvent.ServerPayResult>()
    val serverPayResultLiveData: LiveData<CouponPurchaseEvent.ServerPayResult> =
        _serverPayResultLiveData

    fun pollServerPayResult(orderId: String, shouldRetry: Boolean) {
        viewModelScope.launch {
            if (shouldRetry) {
                var retryTime = 3
                while (retryTime > 0) {
                    val isSuc = requestServerPayResult(orderId)
                    if (isSuc) {
                        _serverPayResultLiveData.value = CouponPurchaseEvent.ServerPayResult(true)
                        return@launch
                    } else {
                        retryTime--
                        delay(2000)
                    }
                }
                _serverPayResultLiveData.value = CouponPurchaseEvent.ServerPayResult(false)
            } else {
                _serverPayResultLiveData.value = CouponPurchaseEvent.ServerPayResult(
                    requestServerPayResult(orderId)
                )
            }
        }
    }

    private suspend fun requestServerPayResult(orderId: String): Boolean {
        val result = BizService.serverPayResult(
            ServerPayResultLocal(orderId)
        )
        return result.isSuccess && result.getOrNull()?.data?.order_status == 99
    }
}

sealed class CouponPurchaseEvent {
    data class ServerPayResult(val isSuccess: Boolean) : CouponPurchaseEvent()
}