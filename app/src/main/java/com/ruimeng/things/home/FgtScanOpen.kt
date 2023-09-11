package com.ruimeng.things.home

import android.os.Bundle
import com.ruimeng.things.R
import wongxd.base.BaseBackFragment
import wongxd.http

class FgtScanOpen : BaseBackFragment() {

    companion object{
        fun  newInstance(deviceId: String,code :String) : FgtScanOpen{
            val fgt = FgtScanOpen()
            val b = Bundle()
            b.putString("deviceId", deviceId)
            b.putString("code", code)
            fgt.arguments = b
            return fgt
        }
    }
    val deviceId: String by lazy { arguments?.getString("deviceId") ?: "" }
    val code: String by lazy { arguments?.getString("code") ?: "" }


    override fun getLayoutRes(): Int = R.layout.fgt_scan_open
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "控制")
        checkPayment()
    }

    private fun checkPayment(){
        http{
            url = "/apiv6/payment/checkpayment"
            params["user_id"] = ""
            params["device_id"] = ""
            params["code"] = ""
            onSuccessWithMsg { res, msg ->

            }
        }
    }
}