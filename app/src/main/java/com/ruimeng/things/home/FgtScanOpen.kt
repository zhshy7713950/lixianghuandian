package com.ruimeng.things.home

import android.os.Bundle
import android.text.TextUtils
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.CheckPaymentBean
import com.utils.TextUtil
import kotlinx.android.synthetic.main.fgt_scan_open.*
import wongxd.base.BaseBackFragment
import wongxd.common.toPOJO
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
    var checkPayBean: CheckPaymentBean.Data? = null


    override fun getLayoutRes(): Int = R.layout.fgt_scan_open
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "控制")
        checkPayment()
    }

    private fun checkPayment(){
        http{
            url = "/apiv6/payment/checkpayment"
            params["user_id"] = "${InfoViewModel.getDefault().userInfo.value?.id}"
            params["device_id"] = deviceId
            params["code"] = code
            onSuccessWithMsg { res, msg ->
                checkPayBean = res.toPOJO<CheckPaymentBean>().data
            }
        }
    }
    private fun showView(){
        var textColors = arrayOf("#929FAB","#FFFFFF")
        if (checkPayBean != null){
            tv_battery_num_pay_rent_money.text = checkPayBean!!.device_id
            tv_battery_model_pay_rent_money.text = checkPayBean!!.device_model
            tv_base_package.text = checkPayBean!!.paymentInfo.pname
            tv_package_start_time.text = TextUtil.getSpannableString(arrayOf("开始时间：",getTimeShow(checkPayBean!!.begin_time)),textColors)
            tv_package_end_time.text = TextUtil.getSpannableString(arrayOf("结束时间：",getTimeShow(checkPayBean!!.exp_time)),textColors)
        }
    }
    private fun getTimeShow(time:String):String{
        return if (time.length > 10 ) time.substring(0,10) else time
    }
}