package com.ruimeng.things.home

import android.os.Bundle
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.FgtMain
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.GetRentPayBean
import com.ruimeng.things.home.bean.LateFeePayBean
import com.ruimeng.things.home.bean.UserLateFeeBean
import com.ruimeng.things.voice.VoicePlayerManager
import com.utils.TextUtil
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_pay_late_fee.*
import kotlinx.android.synthetic.main.fgt_pay_replacement_times.tv_battery_model_pay_rent_money
import kotlinx.android.synthetic.main.fgt_pay_replacement_times.tv_battery_num_pay_rent_money
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http
import java.text.DecimalFormat
import kotlin.math.abs

/**
 * 支付逾期费用
 */
class FgtPayLateFee : BaseBackFragment() {

    companion object {
        const val TAG = "FgtPayLateFeeTag"
        fun newInstance(
        ): FgtPayLateFee {
            return FgtPayLateFee()
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_pay_late_fee
    private var userLateFee: UserLateFeeBean.Data? = null

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "支付逾期费用")
        dealPayWay()
        initView()
        getLateFee()
    }

    private fun getLateFee(){
        http {
            url = Path.GET_USER_LATE_FEE
            params["userId"] = FgtHome.userId
            params["contractId"] = FgtHome.contractId
            onSuccess { rsp ->
                userLateFee = rsp.toPOJO<UserLateFeeBean>().data?.also {
                    tv_total_price.text =
                        TextUtil.getMoneyText("${it.actualLateFee}")
                    tvLateFeeExplain.text ="1.逾期3天内，不收取逾期费用；\n2.逾期超过3天，将按${it.perDayFee}元/天，从第1天开始计算逾期费用；\n3.逾期超过10天，将扣除全部押金，并继续计算逾期费用；\n4.逾期超过30天，我公司将提交法务处理，并继续计算逾期费用"
                    tvLateFee.text = "${it.actualLateFee}元"
                    tvLateDays.text = "${it.actualLateDays}天"
                }
            }
        }
    }

    private fun initView() {
        tv_battery_num_pay_rent_money.text = FgtHome.CURRENT_DEVICEID
        tv_battery_model_pay_rent_money.text = FgtHome.modelName
        btnPayNow.setOnClickListener {
            countPay()
        }
    }

    private fun dealPayWay() {
        rgPayRent.setOnCheckedChangeListener { group, id ->
            when (id) {
                R.id.rbWx -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.WX
                R.id.rbAlipay -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.AL
            }
        }
    }

    private var dlgPayProgress: SweetAlertDialog? = null

    private var dlgPaySuccessed: SweetAlertDialog? = null

    private var dlgPayFailed: SweetAlertDialog? = null

    private var retryTime = 0

    /**
     * 获取服务器上的支付结果
     */
    private fun getServerPayResult(orderId: String, shouldRetry: Boolean) {

        fun dealShouldRetry() {
            if (retryTime <= 3 && shouldRetry) {
                btnPayNow?.postDelayed({
                    getServerPayResult(orderId, shouldRetry)
                }, 2000)
            } else {
                // 播放失败语音
                VoicePlayerManager.getInstance().playVoice(requireContext(), "fail-1")
                dlgPayProgress?.dismiss()
                dlgPayFailed?.show()
            }
        }

        retryTime++

        http {
            IS_SHOW_MSG = false
            url = Path.ORDERSTATUS

            params["orderid"] = orderId

            onSuccess {
                retryTime = 0

                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val order_status = data.optInt("order_status")
                //order_status itn 0待支付1支付失败 99支付成功 100已退款  客户端判断errcode=200,并且order_status等于99即可跳入下一步
                when (order_status) {
                    99 -> {
                        // 播放成功语音
                        VoicePlayerManager.getInstance().playVoice(requireContext(), "success-6")
                        dlgPayProgress?.dismiss()
                        dlgPaySuccessed?.show()
                    }

                    0 -> {
                        dealShouldRetry()
                    }

                    else -> {
                        // 播放失败语音
                        VoicePlayerManager.getInstance().playVoice(requireContext(), "fail-2")
                        dlgPayProgress?.dismiss()
                        dlgPayFailed?.show()
                    }
                }
            }

            onFail { i, s ->
                dealShouldRetry()
            }
        }
    }


    /**
     * 支付成功
     */
    private fun paySuccessed() {
        EventBus.getDefault().post(FgtMain.Companion.SwitchTabEvent(0))
        EventBus.getDefault().post(FgtHome.RefreshMyDeviceList())
        pop()
    }


    private var PAY_WAY_TAG = FgtDeposit.Companion.PayWay.WX

    private fun countPay() {
        if(userLateFee == null) return
        dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
        dlgPaySuccessed =
            getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") { paySuccessed() }
        dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")
        dlgPayProgress?.show()
        http {
            url = Path.PAY_LATE_FEE
            params["userId"] = FgtHome.userId
            params["deviceId"] = FgtHome.CURRENT_DEVICEID
            //支付方式 1微信支付2支付宝支付3白条4免息支付99线下现金100套餐订单101支付宝预授权
            params["payType"] = if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.WX) "1"
            else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.AL) "2"
            else "99"

            onFail { code, msg ->
                dlgPayProgress?.dismiss()
                dlgPayFailed?.apply {
                    this.contentText = msg
                    show()
                }
                // 播放失败语音
                VoicePlayerManager.getInstance().playVoice(requireContext(), "fail-1")
            }

            onSuccessWithMsg { s, msg ->
                val result = s.toPOJO<LateFeePayBean>().data
                if(abs(result.lateFee - userLateFee!!.actualLateFee) > 0.001){
                    dlgPayProgress?.dismiss()
                    NormalDialog(activity)
                        .apply {
                            style(NormalDialog.STYLE_TWO)
                            btnNum(1)
                            title("提示")
                            content("当前逾期费用已发生变化，请在页面刷新后重新支付")
                            btnText("确定")
                            setOnBtnClickL(OnBtnClickL {
                                dismiss()
                                getLateFee()
                            })

                        }.show()
                    return@onSuccessWithMsg
                }

                when (PAY_WAY_TAG) {
                    FgtDeposit.Companion.PayWay.WX -> {
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

                        WXEntryActivity.wxPay(
                            activity,
                            entity,
                            object : WXEntryActivity.WxCallback {
                                override fun onsuccess(code: String?, msg: String?) {
                                    getServerPayResult(result.orderid, true)
                                }

                                override fun onFail(msg: String?) {
                                    getServerPayResult(result.orderid, false)
                                }
                            })
                    }

                    FgtDeposit.Companion.PayWay.AL -> {

                        BaseAlipay.tryPay(result.alipay.paystr) { resultInfo, resultStatus, isLocalSuccessed ->
                            getServerPayResult(result.orderid, isLocalSuccessed)
                        }

                    }

                    else -> {
                        dlgPayProgress?.dismiss()
                        dlgPaySuccessed?.show()
                    }
                }
            }

            onFinish {
                btnPayNow.postDelayed({
                    dlgPayProgress?.dismissWithAnimation()
                }, 10 * 1000)
            }
        }

    }
}
