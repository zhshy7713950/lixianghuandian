package com.ruimeng.things.me

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.flyco.roundview.RoundTextView
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.FgtMain
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.bean.ChangeRentBatteryBean
import com.ruimeng.things.home.bean.ChangeRentBatteryPayInfoBean
import com.ruimeng.things.home.bean.PaymentDetailBean
import com.ruimeng.things.showConfirmDialog
import com.utils.TextUtil
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_battery_hole
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_battery_num
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_battery_status
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_deposit_return
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_remark
import org.greenrobot.eventbus.EventBus
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2020/1/8.
 */
class FgtMeDeposit : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_me_deposite


    companion object {
        fun newInstance(): FgtMeDeposit {
            return FgtMeDeposit().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "账号押金")

        getInfo()
    }


    private fun getInfo() {
        tv_battery_num.text = "电池编号："+FgtHome.CURRENT_DEVICEID
        var textColors = arrayOf("#929FAB","#FFFFFF")
        var type = when (FgtHome.payType){
            "1"->"微信支付"
            "2"->"支付宝支付"
            "101"->"芝麻信用(免押)"
            else -> ""
        }
        var deposit = when (FgtHome.payType){
            "1"->FgtHome.deposit.toString()
            "2"->FgtHome.deposit.toString()
            "101"->"已免押"
            else -> ""
        }


        tv_battery_status.text = TextUtil.getSpannableString(arrayOf("支付渠道：",type),textColors)
        tv_battery_hole.text = TextUtil.getSpannableString(arrayOf("电池押金：",deposit),textColors)

        if (FgtHome.payType == "101"){
            tv_remark.text = "退还电池成功后将自动解除免押绑定"
        }
        tv_deposit_return.setOnClickListener {
            http {
                url = "/apiv6/payment/getuserpaymentinfo"
                params["user_id"] = FgtHome.userId
                params["device_id"] = FgtHome.CURRENT_DEVICEID
                IS_SHOW_MSG = false
                onSuccess { res ->
                   var  paymentDetailBean = res.toPOJO<PaymentDetailBean>().data
                    tryReturnDeposit(paymentDetailBean.contract_id)
                }
            }

        }

    }
    private fun tryReturnDeposit(contractId: String) {

        fun doNetReq() {
            http {

                url = PathV3.RETURN_DEPOIST
                params["contract_id"] = contractId

                onSuccessWithMsg { res, msg ->
                    if (null != activity) {
                        FgtHome.CURRENT_DEVICEID = ""
                        NormalDialog(activity)
                            .apply {
                                style(NormalDialog.STYLE_TWO)
                                btnNum(1)
                                title("提示")
                                content(msg)
                                btnText("确认")
                                setOnBtnClickL(OnBtnClickL {
                                    dismiss()
                                    EventBus.getDefault().post(FgtHome.RefreshMyDeviceList())
                                    EventBus.getDefault().post(FgtMe.RefreshMe())
                                    pop()
                                })

                            }.show()
                    } else {
                        EasyToast.DEFAULT.show(msg)
                    }

                }


                onFail { code, msg ->
                    EasyToast.DEFAULT.show(msg)
                }

            }
        }

        NormalDialog(activity)
            .apply {
                style(NormalDialog.STYLE_TWO)
                btnNum(2)
                title("押金退还结束后，剩余套餐将清零，请确认操作！")
                content("是否确认退还押金？")
                btnText("确认", "取消")
                setOnBtnClickL(OnBtnClickL {
                    dismiss()
                    doNetReq()
                }, OnBtnClickL {
                    dismiss()
                })

            }.show()

    }

}