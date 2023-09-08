package com.ruimeng.things.me.credit

import android.os.Bundle
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_credit_first_pay.*
import org.greenrobot.eventbus.EventBus
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2019/2/20.
 */
class FgtCreditFirstPay : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_credit_first_pay


    companion object {
        fun newInstance(contractId: String, money: String): FgtCreditFirstPay {
            val fgt = FgtCreditFirstPay()
            val b = Bundle()
            b.putString("contractId", contractId)
            b.putString("money", money)
            fgt.arguments = b
            return fgt
        }
    }

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }
    private val money: String by lazy { arguments?.getString("money") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "首付支付")


        tv_money_credit_first_pay.text = "¥  $money"

        btn_alipay_credit_first_pay.setOnClickListener {
            http {
                url = Path.GETFIRSTPAYINFO
                params["contract_id"] = contractId
                params["pay_type"] = "2"
                onSuccess { res -> pay(res, 2) }
            }
        }

        btn_wechat_pay_credit_first_pay.setOnClickListener {
            http {
                url = Path.GETFIRSTPAYINFO
                params["contract_id"] = contractId
                params["pay_type"] = "1"
                onSuccess { res -> pay(res, 1) }
            }
        }
    }


    private var dlgPayProgress: SweetAlertDialog? = null

    private var dlgPaySuccessed: SweetAlertDialog? = null

    private var dlgPayFailed: SweetAlertDialog? = null

    private fun pay(res: String, type: Int) {

        dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
        dlgPaySuccessed = getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") {
            EventBus.getDefault().post(FgtHome.RefreshMyDeviceList())
            pop()
        }
        dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")

        dlgPayProgress?.show()

        // {"errcode":200,"errmsg":"操作成功","data":{"alipay":{"paystr":"1547024021ALIPAY"}}}
//        val json = JSONObject(res)
//        val data = json.optJSONObject("data")
//        val result = data.optJSONObject("alipay")
        val result = res.toPOJO<FgtCreditReckoning.PayInfoBean>().data

        if (type == 1) {
            val entity = WXEntryActivity.WxPayEntity()
            result.wxpay.let {
                entity.appId = it.appId
                entity.nonceStr = it.nonceStr
                entity.packageValue = it.packageValue
                entity.partnerId = it.partnerId
                entity.prepayId = it.prepayId
                entity.sign = it.sign
                entity.timeStamp = it.timeStamp
            }

            WXEntryActivity.wxPay(activity, entity, object : WXEntryActivity.WxCallback {
                override fun onsuccess(code: String?, msg: String?) {
                    dlgPayProgress?.dismissWithAnimation()
                    dlgPaySuccessed?.show()
                }

                override fun onFail(msg: String?) {
                    dlgPayProgress?.dismissWithAnimation()
                    dlgPayFailed?.show()
                }
            })
        } else {
            BaseAlipay.tryPay(result.alipay.paystr) { resultInfo, resultStatus, isLocalSuccessed ->
                dlgPayProgress?.dismissWithAnimation()
                if (isLocalSuccessed) {
                    dlgPaySuccessed?.show()
                } else {
                    dlgPayFailed?.show()
                }
            }
        }

    }
}