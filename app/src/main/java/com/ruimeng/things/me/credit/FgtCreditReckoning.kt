package com.ruimeng.things.me.credit

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.me.credit.bean.CreditReckoningInfoBean
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_credit_reckoning.*
import org.json.JSONObject
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.bothNotNull
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2019/1/2.
 *
 * 信用账单
 *
 */
class FgtCreditReckoning : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_credit_reckoning

    companion object {
        fun newInstance(contractId: String, business_id: String = ""): FgtCreditReckoning {
            val fgt = FgtCreditReckoning()
            val b = Bundle()
            b.putString("contractId", contractId)
            b.putString("business_id", business_id)
            fgt.arguments = b
            return fgt
        }
    }

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }
    private val business_id: String by lazy { arguments?.getString("business_id") ?: "" }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "信用账单")

        rv_credit_reckoning.layoutManager = LinearLayoutManager(activity)
        rv_credit_reckoning.adapter = adapter

        getInfo()
    }

    private fun getInfo() {
        http {
            url = Path.REPAYMENTLIST
            params["contract_id"] = contractId


            onSuccess { res ->
                rv_credit_reckoning?.let {
                    val data = res.toPOJO<CreditReckoningInfoBean>().data

                    tv_battery_num_credit_reckoning.text = "设备编号：${data.base.deviceId}"
                    tv_battery_model_credit_reckoning.text = "设备型号：${data.base.deviceMode}"
                    tv_rent_long_credit_reckoning.text = "租赁周期：${data.base.loanperiod}个月"
                    tv_sign_status_credit_reckoning.text = "签约状态：${data.base.curPeriod}"
                    tv_next_pay_time_credit_reckoning.text = "下一还款日：${data.base.nextDay}"

                    adapter.setNewData(data.list)

                    if (business_id.isNotBlank()) {
                        paySingle(business_id)
                    }
                }
            }


        }
    }

    private val adapter: RvCreditReckoningAdapter by lazy { RvCreditReckoningAdapter() }

    inner class RvCreditReckoningAdapter :
        BaseQuickAdapter<CreditReckoningInfoBean.Data.X, BaseViewHolder>(R.layout.item_rv_credit_reckoning) {
        override fun convert(helper: BaseViewHolder, item: CreditReckoningInfoBean.Data.X?) {
            bothNotNull(helper, item) { a, b ->

                var tips = ""
                if (b.status == 0) {
                    tips = "还款日：状态错误，无法还款"
                } else if (b.status == 1) {
                    tips = "还款日：尚未还款"
                } else {
                    tips = "还款日:${b.day}"
                }
                a.setText(R.id.tv_rent_cycle, "周期：${b.curPeriod}期")
                    .setText(R.id.tv_pay_money, "本期还款：¥${b.loanamount}")
                    .setText(R.id.tv_next_pay_time, tips)
                    .setVisible(R.id.tv_status, b.status == 99)
                    .setVisible(R.id.btn, b.status == 1)
                a.getView<QMUIRoundButton>(R.id.btn)
                    .setOnClickListener {
                        paySingle(b.business_id)
                    }
            }
        }

    }


    private var dlgPayProgress: SweetAlertDialog? = null

    private var dlgPaySuccessed: SweetAlertDialog? = null

    private var dlgPayFailed: SweetAlertDialog? = null


    private fun paySingle(business_id: String) {

        http {
            url = Path.REPAYMENTINFO
            params["contract_id"] = contractId
            params["business_id"] = business_id

            onSuccess {
                //{"errcode":200,"errmsg":"操作成功","data":{"amount":238}}
                val data = JSONObject(it).optJSONObject("data")
                val amount = data.optString("amount")
                showSingePayLayer(business_id, amount)
            }

        }
    }

    private fun showSingePayLayer(business_id: String, amount: String) {

        AnyLayer.with(activity!!)
            .contentView(R.layout.layout_pay_single_credit_reckoning)
            .backgroundBlurRadius(8f)
            .backgroundBlurScale(8f)
            .cancelableOnTouchOutside(false)
            .bindData { anyLayer ->
                val tv = anyLayer.getView<TextView>(R.id.tv)
                val ll_wechat = anyLayer.getView<LinearLayout>(R.id.ll_wechat)
                val ll_alipay = anyLayer.getView<LinearLayout>(R.id.ll_alipay)
                val iv_check_wechat = anyLayer.getView<ImageView>(R.id.iv_check_wechat)
                val iv_check_alipay = anyLayer.getView<ImageView>(R.id.iv_check_alipay)
                val btn = anyLayer.getView<QMUIRoundButton>(R.id.btn)

                tv.text = "本期应还：¥$amount"

                var pay = 2 //	支付方式1微信支付2支付宝支付99线下支付

                iv_check_wechat.visibility = View.GONE

                ll_alipay.setOnClickListener {
                    pay = 2
                    iv_check_wechat.visibility = View.GONE
                    iv_check_alipay.visibility = View.VISIBLE
                }

                ll_wechat.setOnClickListener {
                    pay = 1
                    iv_check_wechat.visibility = View.VISIBLE
                    iv_check_alipay.visibility = View.GONE
                }

                btn.setOnClickListener {
                    http {
                        url = Path.REPAYMENT
                        params["contract_id"] = contractId
                        params["business_id"] = business_id
                        params["pay_type"] = pay.toString()

                        onSuccessWithMsg { res, msg ->


                            dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
                            dlgPaySuccessed = getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") {
                                anyLayer.dismiss()
                                getInfo()
                            }
                            dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")

                            dlgPayProgress?.show()

                            // {"errcode":200,"errmsg":"操作成功","data":{"alipay":{"paystr":"1547024021ALIPAY"}}}
                            val result = res.toPOJO<PayInfoBean>().data
                            if (pay == 1) {
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
                }

            }
            .show()

    }


    data class PayInfoBean(
        var `data`: Data = Data(),
        var errcode: Int = 0, // 200
        var errmsg: String = "" // 操作成功
    ) {
        data class Data(
            var alipay: Alipay = Alipay(),
            var wxpay: Wxpay = Wxpay()
        ) {
            data class Wxpay(
                var appId: String = "", // 1
                var nonceStr: String = "", // 5
                var packageValue: String = "", // 4
                var partnerId: String = "", // 2
                var prepayId: String = "", // 3
                var sign: String = "", // 7
                var timeStamp: String = "" // 6
            )

            data class Alipay(
                var paystr: String = "" // 1542165966
            )
        }


    }
}