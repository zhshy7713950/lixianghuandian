package com.ruimeng.things.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import com.ruimeng.things.FgtMain
import com.ruimeng.things.Path
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.home.adapter.BasePackageAdapter
import com.ruimeng.things.home.adapter.ChangePackageAdapter
import com.ruimeng.things.home.bean.GetRentBean
import com.ruimeng.things.home.bean.GetRentPayBean
import com.ruimeng.things.home.bean.NewGetRentBean
import com.ruimeng.things.me.FgtTicket
import com.ruimeng.things.me.bean.MyCouponBean
import com.ruimeng.things.me.credit.FgtCreditSystem
import com.ruimeng.things.wxapi.WXEntryActivity
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.aty_order.refresh
import kotlinx.android.synthetic.main.fgt_pay_rent_money.*
import kotlinx.android.synthetic.main.fgt_ticket.rv_ticket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.recycleview.yaksa.linear
import wongxd.common.toPOJO
import wongxd.http


/**
 * Created by wongxd on 2018/11/13.
 */
class FgtPayRentMoney : BaseBackFragment() {

    data class EventInstallmentPaymentSuccess(val isNeedPop: Boolean = false)

    companion object {
        fun newInstance(deviceId: String): FgtPayRentMoney {
            val fgt = FgtPayRentMoney()
            val b = Bundle()
            b.putString("deviceId", deviceId)
            fgt.arguments = b
            return fgt
        }
    }

    val deviceId: String by lazy { arguments?.getString("deviceId") ?: "" }
    private val basePackageAdapter: BasePackageAdapter by lazy { BasePackageAdapter() }
    private val changePackageAdapter: ChangePackageAdapter by lazy { ChangePackageAdapter() }

    override fun getLayoutRes(): Int = R.layout.fgt_pay_rent_money


    private var IS_CHECKED_PROTOCOL = false

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        EventBus.getDefault().register(this)
        initTopbar(topbar, "支付租金")


        getRent()

        dealIsBuyInsurance(false)
    }


    @Subscribe
    fun getEventInstallmentPaymentSuccess(event: EventInstallmentPaymentSuccess) {
        Log.d("w-", "getEventInstallmentPaymentSuccess:${event.isNeedPop}")
        if (event.isNeedPop) {
            popChild()
            pop()
        }
    }


    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    /**
     * 选中的租借时长 id
     */
    private var selectedRentLongId = ""

    /**
     * 选中的租借时长 带主机价格
     */
    private var priceHost = ""

    /**
     * 选中的租借时长 不带主机价格
     */
    private var price = ""

    /**
     * 经销商代码
     */
    private var agnetCode = ""


    /**
     * 退押金
     */
    private fun tryReturnDeposit(contractId: String) {

        fun doNetReq() {
            http {

                url = PathV3.RETURN_DEPOIST
                params["contract_id"] = contractId

                onSuccessWithMsg { res, msg ->
                    if (null != activity) {
                        NormalDialog(activity)
                            .apply {
                                style(NormalDialog.STYLE_TWO)
                                btnNum(1)
                                title("提示")
                                content(msg)
                                btnText("确认")
                                setOnBtnClickL(OnBtnClickL {
                                    dismiss()
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
                title("请阅读后点击确认!")
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
    var selectPos = 0
    private fun initViewAfterData(list: List<NewGetRentBean.Data>) {
        if (list.isEmpty()){
            ToastHelper.shortToast(context,"没有设备")
            return
        }
        tv_battery_num_pay_rent_money.text = list.get(0).device_id
        tv_battery_model_pay_rent_money.text = list.get(0).model_name
//        list.forEach{it.options[]}
        basePackageAdapter.setNewData(list)
        changePackageAdapter.setNewData(list.get(0).options)
        rv_rant_long_pay_rent_money.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)
        rv_rant_long_pay_rent_money.adapter = basePackageAdapter

        basePackageAdapter.setOnItemClickListener(object :BaseQuickAdapter.OnItemClickListener{
            override fun onItemClick(p0: BaseQuickAdapter<*, *>?, p1: View?, p2: Int) {
                basePackageAdapter.selectPos = p2
                basePackageAdapter.notifyDataSetChanged()
                changePackageAdapter.setNewData(list.get(p2).options)
            }
        })
        rv_change_package.layoutManager = GridLayoutManager(activity,2)
        rv_change_package.adapter = changePackageAdapter

//        agnetCode = data.device_info.code

//        tv_combination_pay_rent_money.text = data.deposit_option_label
//
//        qfl_change_combination_pay_rent.setOnClickListener {
//            startWithPop(FgtChangeDepositCombination.newInstance(data.contract_id))
//        }
//
//        btn_return_deposit_pay_rent_money.visibility =
//            if (data.btn_return == 1) View.VISIBLE else View.GONE
//
//        btn_return_deposit_pay_rent_money.setOnClickListener {
//            tryReturnDeposit(data.contract_id)
//        }

//        tv_agnet_name_pay_rent_money.text = "" + agnetCode
//
//        tv_battery_num_pay_rent_money.text = "当前电池编号：" + data.device_info.device_id
//
//        tv_battery_model_pay_rent_money.text = "当前电池型号：" + data.device_info.device_model
//
//        ll_enter_rant_long_pay_rant_now.setOnClickListener {
//            if (rv_rant_long_pay_rent_money.visibility == View.VISIBLE) {
//                iv_enter_rant_long.rotation = 0f
//                rv_rant_long_pay_rent_money.visibility = View.GONE
//            } else {
//                iv_enter_rant_long.rotation = 90f
//                rv_rant_long_pay_rent_money.visibility = View.VISIBLE
//            }
//
//        }

        fun refrenshWithCheckedRent(item: GetRentBean.Data.Payment) {
//            tv_insurance_money.text = "保费：¥${item.insurance_price}"
//            tv_rant_long_pay_rent_money.text = item.name
//            selectedRentLongId = item.id
//            price = item.price
//            priceHost = item.price_host
//
//            tv_money_pay_rent_money.text = if (data.device_info.is_host == "1") priceHost else price
        }

//        refrenshWithCheckedRent(data.payment[0])




//        ll_wechat_pay_rent_money.setOnClickListener {
//            dealPayWay(FgtDeposit.Companion.PayWay.WX)
//        }
//
//        ll_alipay_pay_rent_money.setOnClickListener {
//            dealPayWay(FgtDeposit.Companion.PayWay.AL)
//        }
//
//        ll_installment_pay_rent_money.setOnClickListener {
//            dealPayWay(FgtDeposit.Companion.PayWay.FQ)
//        }
//
//        ll_credit_pay_rent_money.setOnClickListener {
//            dealPayWay(FgtDeposit.Companion.PayWay.BT)
//        }
//        groupPayLayout?.setOnClickListener {
//            dealPayWay(FgtDeposit.Companion.PayWay.GROUPPAP)
//        }
//
//        ll_cash_pay_rent_money.setOnClickListener {
//            dealPayWay(FgtDeposit.Companion.PayWay.XX)
//        }
//
//
//
//        ll_wechat_pay_rent_money.visibility = View.GONE
//        ll_alipay_pay_rent_money.visibility = View.GONE
//        ll_cash_pay_rent_money.visibility = View.GONE
//        ll_installment_pay_rent_money.visibility = View.GONE
//        ll_credit_pay_rent_money.visibility = View.GONE
//        groupPayLayout.visibility = View.GONE



//        ll_enter_ticket_pay_rent_money.setOnClickListener {
//
//            if (!IS_TICKET_DATA_INIT) {
//                EasyToast.DEFAULT.show("正在获取优惠券信息")
//                initTicket()
//                return@setOnClickListener
//            } else
//                pvOptions?.show()
//
//        }


        initTicket()


//        btn_yes_buy_insurance.setOnClickListener { dealIsBuyInsurance(true) }
//        btn_no_buy_insurance.setOnClickListener { dealIsBuyInsurance(false) }

        btn_pay_now_pay_rent_money.setOnClickListener { view ->

            if (!IS_CHECKED_PROTOCOL) {
                EasyToast.DEFAULT.show("请先同意协议")
                return@setOnClickListener
            }


            if (agnetCode.isBlank()) {
                EasyToast.DEFAULT.show("请先输入经销商信息")
                return@setOnClickListener
            }

            if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.NULL) {
                EasyToast.DEFAULT.show("请选择支付方式")
                return@setOnClickListener
            }


            dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
            dlgPaySuccessed =
                getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") { paySuccessed() }
            dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")


            dlgPayProgress?.show()

            http {
//                url = Path.GET_RENT_PAY
                url = "apiv5/getrentpay"
                params["device_id"] = deviceId
                params["pack_id"] = selectedRentLongId
                //支付方式 1微信支付2支付宝支付3白条4免息支付99线下现金100套餐订单101支付宝预授权
                params["pay_type"] = if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.WX) "1"
                else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.AL) "2"
                else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.BT) "3"
                else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.FQ) "4"
                else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.GROUPPAP) "102"
                else "99"

                params["coupon_id"] = couponId
                params["code"] = agnetCode
//                params["insurance"] = if (isBuyInsurance) "1" else "0"


                onFail { code, msg ->
                    dlgPayProgress?.dismiss()
                    dlgPayFailed?.apply {
                        this.contentText = msg
                        show()
                    }
                }

                onSuccessWithMsg { s, msg ->
                    //{"errcode":200,"errmsg":"\u64cd\u4f5c\u6210\u529f","data":{"wxpay":{"appId":"1","partnerId":"2","prepayId":"3","packageValue":"4","nonceStr":"5","timeStamp":"6","sign":"7"},"alipay":{"paystr":1542265595},"orderid":"10c2c019-b4b8-d764-48ba-cb573865b080"}}
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
                        FgtDeposit.Companion.PayWay.BT -> {
                            dlgPayProgress?.dismiss()
                            EventBus.getDefault().post(BatteryInfoChangeEvent(deviceId))
                            EventBus.getDefault().post(FgtMain.Companion.SwitchTabEvent(0))
                            startWithPop(FgtCreditSystem())
                        }
//                        FgtDeposit.Companion.PayWay.FQ -> {
//                            dlgPayProgress?.dismiss()
//                            start(
//                                FgtRentInstallmentPayment.newInstance(
//                                    data.contract_id,
//                                    result.orderid
//                                )
//                            )
//                        }
                        FgtDeposit.Companion.PayWay.GROUPPAP -> {
                            dlgPayProgress?.dismiss()
                            dlgPaySuccessed?.show()
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


        iv_check_pay_rent_money.setOnClickListener {
            if (IS_CHECKED_PROTOCOL) {
                iv_check_pay_rent_money.setImageResource(R.drawable.uncheck)
            } else {
                iv_check_pay_rent_money.setImageResource(R.drawable.check)
            }

            IS_CHECKED_PROTOCOL = !IS_CHECKED_PROTOCOL
        }


        tv_view_rant_protocol_pay_rent_money.setOnClickListener {
            val dlg = DialogFragmentRentProtocol()
            dlg.show(childFragmentManager, "protocol")
        }


        iv_check_pay_rent_money.performClick()
//        dealPayWay(FgtDeposit.Companion.PayWay.NULL)
    }




    private var isBuyInsurance = false

    /**
     * 是否购买保险
     */
    private fun dealIsBuyInsurance(isBuy: Boolean) {
        isBuyInsurance = isBuy

//        tv_insurance_money_tip.visibility = if (isBuy) View.VISIBLE else View.GONE
//        tv_insurance_money.visibility = if (isBuy) View.VISIBLE else View.GONE

        fun setBtnStatus(isCheck: Boolean, btn: QMUIRoundButton) {

            val appColor = activity?.resources?.getColor(R.color.app_color)!!
            val btnDrawable: QMUIRoundButtonDrawable = btn.background as QMUIRoundButtonDrawable

            if (isCheck) {
                btnDrawable.setStrokeData(1, ColorStateList.valueOf(appColor))
                btnDrawable.setBgData(ColorStateList.valueOf(appColor))
                btn.setTextColor(Color.WHITE)
            } else {
                btnDrawable.setStrokeData(1, ColorStateList.valueOf(Color.WHITE))
                btnDrawable.setBgData(ColorStateList.valueOf(Color.WHITE))
                btn.setTextColor(appColor)
            }
        }

//        setBtnStatus(isBuy, btn_yes_buy_insurance)
//        setBtnStatus(!isBuy, btn_no_buy_insurance)
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


    /**
     * 选中的优惠券的id
     */
    private var couponId = ""

    /**
     * 是否从网络中获取到了优惠券信息
     */
    private var IS_TICKET_DATA_INIT = false

    private var pvOptions: OptionsPickerView<MyCouponBean.Data>? = null

    private fun initTicket() {

        http {
            url = Path.GET_MY_COUPON
            params["device_id"] = deviceId

            onSuccess {
                IS_TICKET_DATA_INIT = true
                val result = it.toPOJO<MyCouponBean>().data
                val list = mutableListOf<MyCouponBean.Data>()
                list.add(MyCouponBean.Data("", "不使用优惠券", "", "0(不使用)"))
                list.addAll(result)


                //条件选择器
                pvOptions = OptionsPickerBuilder(activity,
                    OnOptionsSelectListener { options1, option2, options3, v ->
                        //返回的分别是三个级别的选中位置
                        val item = list[options1]
                        tv_ticket_pay_rent_money.text = item.coupon_label
                        couponId = item.coupon_id
                        tv_ticket_pay_rent_money.performClick()
                    })
                    .build<MyCouponBean.Data>()
                pvOptions?.setPicker(list)
            }


            onFail { i, s ->
                IS_TICKET_DATA_INIT = false
            }

        }
    }

    private var PAY_WAY_TAG = FgtDeposit.Companion.PayWay.NULL

//    private fun dealPayWay(tag: FgtDeposit.Companion.PayWay) {
//
//        PAY_WAY_TAG = tag
//
//        fun isViewShow(v: View, isShow: Boolean) {
//            if (isShow) {
//                v.visibility = View.VISIBLE
//            } else {
//                v.visibility = View.GONE
//            }
//        }
//
//        isViewShow(iv_check_wechat_pay_rent_money, false)
//        isViewShow(iv_check_alipay_pay_rent_money, false)
//        isViewShow(iv_check_installment_pay_rent_money, false)
//        isViewShow(iv_check_credit_pay_rent_money, false)
//        isViewShow(groupPayImage, false)
//        isViewShow(iv_check_cash_pay_rent_money, false)
//
//        when (tag) {
//
//            FgtDeposit.Companion.PayWay.NULL -> {
//            }
//
//            FgtDeposit.Companion.PayWay.WX -> {
//                isViewShow(iv_check_wechat_pay_rent_money, true)
//            }
//
//            FgtDeposit.Companion.PayWay.AL -> {
//                isViewShow(iv_check_alipay_pay_rent_money, true)
//            }
//
//            FgtDeposit.Companion.PayWay.XX -> {
//                isViewShow(iv_check_cash_pay_rent_money, true)
//            }
//
//            FgtDeposit.Companion.PayWay.FQ -> {
//                isViewShow(iv_check_installment_pay_rent_money, true)
//            }
//
//            FgtDeposit.Companion.PayWay.BT -> {
//                isViewShow(iv_check_credit_pay_rent_money, true)
//            }
//            FgtDeposit.Companion.PayWay.GROUPPAP -> {
//                isViewShow(groupPayImage, true)
//            }
//            else -> {
//
//            }
//        }
//    }

    /**
     * 获取设备租用信息
     */
    private fun getRent() {

        http {
            url = PathV3.GET_RENT
            params["deviceId"] = deviceId

            onSuccessWithMsg { res, msg ->

                iv_battery_pay_rent_money?.let {
                    val result = res.toPOJO<NewGetRentBean>().data
                    try {
                        initViewAfterData(result)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}