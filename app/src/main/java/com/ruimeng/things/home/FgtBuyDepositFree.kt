package com.ruimeng.things.home

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.FgtMain
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.LateFeePayBean
import com.ruimeng.things.utils.PageNavigationHelper
import com.ruimeng.things.voice.VoicePlayerManager
import com.utils.TextUtil
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_buy_deposit_free.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http

/**
 * 购买免押权益
 */
class FgtBuyDepositFree : BaseBackFragment() {

    companion object {
        const val TAG = "FgtBuyDepositFreeTag"
        fun newInstance(deviceId: String, buyFreeAmount: String, buyFreeExpire: String, originalDeposit: String): FgtBuyDepositFree {
            val fgt = FgtBuyDepositFree()
            val bundle = Bundle()
            bundle.putString("deviceId", deviceId)
            bundle.putString("buyFreeAmount", buyFreeAmount)
            bundle.putString("buyFreeExpire", buyFreeExpire)
            bundle.putString("originalDeposit", originalDeposit)
            fgt.arguments = bundle
            return fgt
        }
    }

    private var deviceId = ""
    private var buyFreeAmount = "30.00"
    private var buyFreeExpire = "90"
    private var originalDeposit = "300.00"

    override fun getLayoutRes(): Int = R.layout.fgt_buy_deposit_free

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        
        arguments?.let {
            deviceId = it.getString("deviceId", "")
            buyFreeAmount = it.getString("buyFreeAmount", "30.00")
            buyFreeExpire = it.getString("buyFreeExpire", "90")
            originalDeposit = it.getString("originalDeposit", "300.00")
        }

        initTopbar(topbar, "免押权益")
        dealPayWay()
        initView()
    }

    private fun initView() {
        // Text 1
        tv_desc1.text = getHighlightText(
            "1.免押权益购买后，即可自动抵扣所选型号的单个电池全额押金，无需额外支付押金。",
            "全额押金"
        )
        
        // Text 2
        tv_desc2.text = getHighlightText(
            "2.当前免押权益有效期为${buyFreeExpire}天。",
            "${buyFreeExpire}天"
        )
        
        // Text 3
        tv_desc3.text = getHighlightText(
            "3.若免押权益有效期内您并未购买“换电套餐”，或者中途退租，或者取消“换电套餐”，免押权益将失效。\n若您想继续使用“换电套餐”服务，需按平台规定支付费用",
            "免押权益有效期内您并未购买“换电套餐”",
            "中途退租",
            "取消“换电套餐”"
        )
        
        // Text 4
        tv_desc4.text = getHighlightText(
            "4.免押权益为虚拟权益，有效期内仅能使用1次，购买后不支持退款。",
            "1次",
            "不支持退换"
        )

        // Prices
        tv_total_price.text = TextUtil.getMoneyText(buyFreeAmount)
        tv_original_price.text = TextUtil.getMoneyText(originalDeposit)
        tv_original_price.paintFlags = tv_original_price.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        btnPayNow.setOnClickListener {
            countPay()
        }
    }

    private fun getHighlightText(fullText: String, vararg highlights: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder(fullText)
        val highlightColor = Color.parseColor("#fad44d")
        
        for (highlight in highlights) {
            val start = fullText.indexOf(highlight)
            if (start != -1) {
                val end = start + highlight.length
                builder.setSpan(ForegroundColorSpan(highlightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.setSpan(AbsoluteSizeSpan(15, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return builder
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
    private var retryTime = 0

    private fun getServerPayResult(orderId: String, shouldRetry: Boolean) {

        fun dealShouldRetry() {
            if (retryTime <= 3 && shouldRetry) {
                btnPayNow?.postDelayed({
                    getServerPayResult(orderId, shouldRetry)
                }, 2000)
            } else {
                payFailed()
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
                    99 -> paySuccessed()
                    0 -> dealShouldRetry()
                    else -> payFailed()
                }
            }

            onFail { i, s ->
                dealShouldRetry()
            }
        }
    }

    private fun paySuccessed() {
        dlgPayProgress?.dismiss()
        EasyToast.DEFAULT.show("支付成功")
        VoicePlayerManager.getInstance().playVoice(requireContext(), "success-6") // "success-6" typically success sound

        FgtHome.CURRENT_DEVICEID = deviceId
        PageNavigationHelper.backToMainAndSwitchTab(0,this)
        EventBus.getDefault().post(FgtHome.RefreshMyDeviceList())
        btnPayNow?.postDelayed({
            FgtHome.tryToScan(prefix = AtyScanQrcode.TYPE_PAY_RENT)
        },1500)
    }
    
    private fun payFailed() {
        dlgPayProgress?.dismiss()
        EasyToast.DEFAULT.show("支付失败，请稍后重试")
        VoicePlayerManager.getInstance().playVoice(requireContext(), "fail-1") // "fail-1" typically fail sound
    }

    private var PAY_WAY_TAG = FgtDeposit.Companion.PayWay.WX

    private fun countPay() {
        dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
        dlgPayProgress?.show()
        
        http {
            url = "apiv6/payment/buyfreedeposit"
            params["userId"] = FgtHome.userId
            params["deviceId"] = deviceId
            //支付方式 1微信支付2支付宝支付
            params["payType"] = if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.WX) "1" else "2"

            onFail { code, msg ->
                dlgPayProgress?.dismiss()
                EasyToast.DEFAULT.show(msg)
                VoicePlayerManager.getInstance().playVoice(requireContext(), "fail-1")
            }

            onSuccessWithMsg { s, msg ->
                val result = s.toPOJO<LateFeePayBean>().data
                
                when (PAY_WAY_TAG) {
                    FgtDeposit.Companion.PayWay.WX -> {
                        val entity = WXEntryActivity.WxPayEntity()
                        result.wxpay?.let {
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
                        paySuccessed()
                    }
                }
            }

            onFinish {
                btnPayNow?.postDelayed({
                    dlgPayProgress?.dismissWithAnimation()
                }, 10 * 1000)
            }
        }
    }
}
