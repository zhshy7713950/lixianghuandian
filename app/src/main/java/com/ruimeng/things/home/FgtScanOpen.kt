package com.ruimeng.things.home

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.view.isVisible
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ruimeng.things.FgtMain
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.CheckPaymentBean
import com.ruimeng.things.home.bean.PaymentOption
import com.utils.TextUtil
import com.utils.ToastHelper
import com.utils.safeToInt
import kotlinx.android.synthetic.main.fgt_scan_open.*
import kotlinx.android.synthetic.main.package_details_layout.*
import wongxd.base.BaseBackFragment
import wongxd.common.openInApp
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.ToastUtils

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
        initTopbar(topbar, "扫码开门")
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
                showView()
            }
        }
    }
    private fun showView(){
        var textColors = arrayOf("#929FAB","#FFFFFF")
        if (checkPayBean != null){
            tv_battery_num_pay_rent_money.text = checkPayBean!!.device_id
            tv_battery_model_pay_rent_money.text = checkPayBean!!.modelName
            tv_base_package_name.text = checkPayBean!!.paymentInfo.pname
            tv_base_package_time.text = TextUtil.formatTime(checkPayBean!!.begin_time,checkPayBean!!.exp_time)
            val options = checkPayBean!!.paymentInfo.userOptions.filter { it.option_type == "2" && it.active_status == "1"}
            var needActiveNew = false // 是否需要启用新套餐
//            if (!options.isEmpty()){
            val isUnlimited = checkPayBean?.open_check == 1

            var restTimes = ""
            if (isUnlimited) {
                restTimes = "次数无限制" // 次数无限制
            } else {
                // 获取实际次数
                checkPayBean?.paymentInfo?.userOptions?.let { options ->
                    if (options.isNotEmpty()) {
                        val times = options.first().change_times.safeToInt()
                        restTimes = if (times >= 999) {
                            "次数无限制"
                        }else {
                            "${times}次"
                        }
                    }
                }
            }
            tv_package_remaining_times.text = restTimes
            ll_other_options.isVisible = false

            tv_change_battery.setOnClickListener {
                http {
                    url = "/apiv6/cabinet/openDoor"
                    params["user_id"] = "${InfoViewModel.getDefault().userInfo.value?.id}"
                    params["device_id"] = deviceId
                    params["code"] = code
                    onSuccessWithMsg { res, msg ->
                        ToastHelper.shortToast(activity, msg)
//                        if (needActiveNew){
//                            var option =  checkPayBean!!.paymentInfo.userOptions.filter{ it.option_type == "2" && it.active_status != "1"}.first()
//                            activeOption(option)
//                        }else{
                        tv_change_battery.postDelayed(Runnable { pop() },2000)
//                        }
                    }
                    onFail { i, s ->
                        ToastHelper.shortToast(activity, s)
                    }
                }
            }
        }
    }

    private fun activeOption(option: PaymentOption){
        NormalDialog(activity).apply {
            style(NormalDialog.STYLE_TWO)
            title("当前您的换电套餐剩余次数已为0，是否立即启动待生效套餐：${option.name}")
            titleTextColor(Color.parseColor("#131414"))
            btnText("确认启用", "暂不启用")
            btnTextColor(Color.parseColor("#29EBB6"), Color.parseColor("#FF6464"))
            setOnBtnClickL(OnBtnClickL {
                http {
                    url = "/apiv6/payment/activeoption"
                    params["user_option_id"] = "${option.id}"
                    onSuccess {
                        ToastHelper.shortToast(context,"启用成功")
                        dismiss()
                        pop()
                    }
                    onFail { i, s ->
                        ToastHelper.shortToast(context,s)
                        dismiss()
                    }
                }

            }, OnBtnClickL {
                dismiss()
            })
            show()
        }
    }
    private fun getTimeShow(time:String):String{
        return if (time.length > 10 ) time.substring(0,10) else time
    }
}