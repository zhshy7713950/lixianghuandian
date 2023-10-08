package com.ruimeng.things.home

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.FgtMain
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.GetRentPayBean
import com.ruimeng.things.home.bean.PaymentDetailBean
import com.ruimeng.things.home.bean.PaymentInfo
import com.ruimeng.things.me.contract.FgtContractSignStep1
import com.ruimeng.things.me.credit.FgtCreditSystem
import com.ruimeng.things.wxapi.WXEntryActivity
import com.utils.TextUtil
import kotlinx.android.synthetic.main.fgt_pay_rent_money.btn_pay_now_pay_rent_money
import kotlinx.android.synthetic.main.fgt_pay_rent_money.iv_check_pay_rent_money
import kotlinx.android.synthetic.main.fgt_pay_rent_money.rgPayRent
import kotlinx.android.synthetic.main.fgt_pay_rent_money.tv_view_rant_protocol_pay_rent_money
import kotlinx.android.synthetic.main.fgt_single_pay.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class FgtSinglePay : BaseBackFragment() {
    companion object{
        fun  newInstance(deviceId: String) : FgtSinglePay{
            val fgt = FgtSinglePay()
            val b = Bundle()
            b.putString("deviceId", deviceId)
            fgt.arguments = b
            return fgt
        }
    }
    private var PAY_WAY_TAG = FgtDeposit.Companion.PayWay.WX
    private var IS_CHECKED_PROTOCOL = false
    private var dlgPayProgress: SweetAlertDialog? = null

    private var dlgPaySuccessed: SweetAlertDialog? = null

    private var dlgPayFailed: SweetAlertDialog? = null
    val deviceId: String by lazy { arguments?.getString("deviceId") ?: "" }
    override fun getLayoutRes(): Int = R.layout.fgt_single_pay
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "单次换电")
        getPaymentInfo()
        EventBus.getDefault().register(this)
    }
    private fun getPaymentInfo(){
        http {
            url = "/apiv6/payment/getuserpaymentinfo"
            params["user_id"] = FgtHome.userId
            params["device_id"] = deviceId
            onSuccess {res->
                iv_battery_pay_rent_money?.let {
                    val data = res.toPOJO<PaymentDetailBean>().data
                    showView(data.paymentInfo)

                }
            }
        }
    }
    private fun showView(paymentInfo : PaymentInfo){
        tv_battery_num_pay_rent_money.text = deviceId
        tv_battery_model_pay_rent_money.text = paymentInfo.modelName
        tv_price.text = TextUtil.getMoneyText("${paymentInfo.single_price}")
        tv_total_price.text = TextUtil.getMoneyText( "${paymentInfo.single_price}")
        var formatter = SimpleDateFormat("yyyy-MM-dd")
        var calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH,+1)

        tv_time.text = "有效期：${formatter.format(Date())}至${formatter.format(calendar.time)}"
        rgPayRent.setOnCheckedChangeListener { group, id ->
            when (id) {
                R.id.rbWx -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.WX
                R.id.rbAlipay -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.AL
            }
        }
        iv_check_pay_rent_money.setOnClickListener {
            if (IS_CHECKED_PROTOCOL) {
                iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_unselect)
            } else {
                iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_select)
            }
            IS_CHECKED_PROTOCOL = !IS_CHECKED_PROTOCOL
        }
        tv_view_rant_protocol_pay_rent_money.setOnClickListener {
           start(FgtContractSignStep1.newInstance(FgtHome.contractId,"",0,1,deviceId,paymentInfo.modelName))
        }
        btn_pay_now_pay_rent_money.setOnClickListener {
            if (!IS_CHECKED_PROTOCOL) {
                EasyToast.DEFAULT.show("请先同意协议")
                return@setOnClickListener
            }
            dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
            dlgPaySuccessed =
                getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") { paySuccessed() }
            dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")

            dlgPayProgress?.show()

            http {
                url ="/apiv6/payment/singlechangePay"
                params["user_id"] = FgtHome.userId
                params["device_id"] = deviceId
                params["payType"] = if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.WX) "1" else "2"
                params["price"] = paymentInfo.single_price
                onFail { code, msg ->
                    dlgPayProgress?.dismiss()
                    dlgPayFailed?.apply {
                        this.contentText = msg
                        show()
                    }
                }
                onSuccessWithMsg { s, msg ->
                    val result = s.toPOJO<GetRentPayBean>().data
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
                    btn_pay_now_pay_rent_money.postDelayed({
                        dlgPayProgress?.dismissWithAnimation()
                    }, 10 * 1000)
                }
            }

            }
        }
    private var retryTime = 0
    @Subscribe
    fun checkContract(event: ContractCheckEvent) {
        IS_CHECKED_PROTOCOL = true
        iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_select)
    }
    /**
     * 获取服务器上的支付结果
     */
    private fun getServerPayResult(orderId: String, shouldRetry: Boolean) {

        fun dealShouldRetry() {
            if (retryTime <= 3 && shouldRetry) {
                btn_pay_now_pay_rent_money?.postDelayed({
                    getServerPayResult(orderId, shouldRetry)
                }, 2000)
            } else {
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
                        dlgPayProgress?.dismiss()
                        dlgPaySuccessed?.show()
                    }
                    0 -> {
                        dealShouldRetry()
                    }
                    else -> {
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
        EventBus.getDefault().post(BatteryInfoChangeEvent(deviceId))
        EventBus.getDefault().post(FgtMain.Companion.SwitchTabEvent(0))
        pop()
    }

}