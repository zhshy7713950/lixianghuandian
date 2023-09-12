package com.ruimeng.things.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
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
import com.google.gson.Gson
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import com.ruimeng.things.FgtMain
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.Path
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.bean.BaseResultBean
import com.ruimeng.things.home.adapter.BasePackageAdapter
import com.ruimeng.things.home.adapter.ChangePackageAdapter
import com.ruimeng.things.home.bean.GetRentBean
import com.ruimeng.things.home.bean.GetRentPayBean
import com.ruimeng.things.home.bean.NewGetRentBean
import com.ruimeng.things.me.FgtTicket
import com.ruimeng.things.me.bean.MyCouponBean
import com.ruimeng.things.me.credit.FgtCreditSystem
import com.ruimeng.things.wxapi.WXEntryActivity
import com.utils.OnSinglePickerSelectListener
import com.utils.OptionPickerUtil
import com.utils.TextUtil
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.aty_order.refresh
import kotlinx.android.synthetic.main.fgt_deposit.rgDeposit
import kotlinx.android.synthetic.main.fgt_deposit.tv_select_package_deposit
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
    private var newGetRentBean: NewGetRentBean.Data? = null;
    private var selectOption: NewGetRentBean.Data.Option? = null;

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        EventBus.getDefault().register(this)
        initTopbar(topbar, "支付租金")
        getRent()
        dealPayWay()
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
    private fun dealPayWay() {
        rgPayRent.setOnCheckedChangeListener { group, id ->
            when (id) {
                R.id.rbWx -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.WX
                R.id.rbAlipay -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.AL
                R.id.rbOffline -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.XX
            }
        }
    }
    private fun resetSelectOptionList(){
        var optionList  : ArrayList<NewGetRentBean.Data.Option> = ArrayList()
        optionList.add(NewGetRentBean.Data.Option())
        newGetRentBean?.let {
            optionList.addAll(newGetRentBean!!.options.filter { it.option_type == "2" })
            if (!TextUtils.isEmpty(it.show_start_time) && !TextUtils.isEmpty(it.show_end_time)){
                tv_rant_long_pay_time.text = "有效期："+ formatTime(it.show_start_time) +"至" + formatTime(it.show_end_time)
            }else{
                tv_rant_long_pay_time.text = "无限制"
            }
        }
        changePackageAdapter.setNewData(optionList)
        tv_select_charge.text = "否"
        tv_select_platform.text = "否"
        tv_select_insurance.text = "否"
    }
    private fun formatTime(timeStr: String): Any? {
        return if (TextUtils.isEmpty(timeStr)) ":" else timeStr.substring(0,10)
    }
    private fun initViewAfterData(list: List<NewGetRentBean.Data>) {
        if (list.isEmpty()){
            ToastHelper.shortToast(context,"没有找到套餐")
            return
        }
        newGetRentBean = list.get(0)
        newGetRentBean.let {
            tv_battery_num_pay_rent_money.text = newGetRentBean!!.device_id
            tv_battery_model_pay_rent_money.text = newGetRentBean!!.model_name
            tv_agnet_name_pay_rent_money.text= newGetRentBean!!.agentCode
            et_agnet_name_deposit.text = newGetRentBean!!.agentName
            btn_return_deposit_pay_rent_money.visibility = if (newGetRentBean!!.btn_return == 1) View.VISIBLE else View.GONE
            btn_return_deposit_pay_rent_money.setOnClickListener {
                tryReturnDeposit(newGetRentBean!!.contract_id)
            }
        }



        basePackageAdapter.setNewData(list)
        resetSelectOptionList()
        rv_rant_long_pay_rent_money.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)
        rv_rant_long_pay_rent_money.adapter = basePackageAdapter

        basePackageAdapter.setOnItemClickListener(object :BaseQuickAdapter.OnItemClickListener{
            override fun onItemClick(p0: BaseQuickAdapter<*, *>?, p1: View?, p2: Int) {
                newGetRentBean = list.get(p2)
                basePackageAdapter.selectPos = p2
                basePackageAdapter.notifyDataSetChanged()
                computeAmount()
                resetSelectOptionList()
            }
        })
        rv_change_package.layoutManager = GridLayoutManager(activity,2)
        rv_change_package.adapter = changePackageAdapter
        changePackageAdapter.setOnItemClickListener(object :BaseQuickAdapter.OnItemClickListener{
            override fun onItemClick(p0: BaseQuickAdapter<*, *>?, p1: View?, p2: Int) {
                selectOption = changePackageAdapter.data.get(p2)
                changePackageAdapter.selectPos = p2
                changePackageAdapter.notifyDataSetChanged()
                computeAmount()
                selectOption.let {
                    if (it != null) {
                        if (!TextUtils.isEmpty(it.show_start_time) && !TextUtils.isEmpty(it.show_end_time)) {
                            tv_option_time.text =
                                "有效期：" + formatTime(it.show_start_time) + "至" + formatTime(it.show_end_time)
                        }
                    }
                }

            }
        })
        tv_select_charge.setOnClickListener {
            newGetRentBean.let {
                if (it != null) {
                    val options = it.options.filter {  it.option_type == "5" }
                    if (options != null && options.size > 0){
                        OptionPickerUtil.showSingleOptionPicker(activity, arrayOf("否","是（"+options.get(0).price+"元)").toMutableList())
                        {  tv_select_charge.text = it
                            computeAmount()}
                    }
                }
            }
        }
        tv_select_platform.setOnClickListener {
            newGetRentBean.let {
                if (it != null) {
                    val options = it.options.filter {  it.option_type == "4" }
                    if (options != null && options.size > 0){
                        OptionPickerUtil.showSingleOptionPicker(activity, arrayOf("否","是（"+options.get(0).price+"元)").toMutableList())
                        {  tv_select_platform.text = it
                            computeAmount()}
                    }
                }
            }
        }
        tv_select_insurance.setOnClickListener {
            newGetRentBean.let {
                if (it != null) {
                    val options = it.options.filter {  it.option_type == "3" }
                    options.let {
                        var filters : ArrayList<String> = ArrayList()
                        filters.add("否")
                        options.forEach {
                            filters.add(it.name+"("+it.price+"元)")
                        }
                        OptionPickerUtil.showSingleOptionPicker(activity, filters)
                        {  tv_select_insurance.text = it
                            computeAmount()
                        }
                    }
                }
            }
        }
        computeAmount()

        initTicket()

        btn_pay_now_pay_rent_money.setOnClickListener { view ->

            if (!IS_CHECKED_PROTOCOL) {
                EasyToast.DEFAULT.show("请先同意协议")
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
//                url = "apiv5/getrentpay"
                url = "apiv6/payment/payrentmoney"
                jsonParam  = getSubmitParam()
                //支付方式 1微信支付2支付宝支付3白条4免息支付99线下现金100套餐订单101支付宝预授权
                params["payType"] = if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.WX) "1"
                else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.AL) "2"
                else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.BT) "3"
                else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.FQ) "4"
                else if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.GROUPPAP) "102"
                else "99"



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
                iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_unselect)
            } else {
                iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_select)
            }

            IS_CHECKED_PROTOCOL = !IS_CHECKED_PROTOCOL
        }


        tv_view_rant_protocol_pay_rent_money.setOnClickListener {
            val dlg = DialogFragmentRentProtocol()
            dlg.show(childFragmentManager, "protocol")
        }


        iv_check_pay_rent_money.performClick()
        tv_ticket_pay_rent_money.setOnClickListener {
            pvOptions?.show()
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
    private var couponId = 0

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
                        couponId = item.id
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

    private var PAY_WAY_TAG = FgtDeposit.Companion.PayWay.AL


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
    private fun computeAmount() {
        http {
            url = "/apiv6/payment/computeamount"
            jsonParam = getSubmitParam()
            onSuccessWithMsg { res, msg ->
                val result = res.toPOJO<BaseResultBean<Double>>().data
                tv_total_price.text  = "¥${result}"
            }
        }
    }
    private fun getSubmitParam(): MutableMap<String, Any> {
        var params: MutableMap<String, Any> = mutableMapOf()
        params["deviceId"] = deviceId
        params["user_id"] =  "${InfoViewModel.getDefault().userInfo.value?.id}"
        params["couponId"] = "${couponId}"

        if (newGetRentBean != null) {
            params["payment_id"] = newGetRentBean!!.id
            params["package_id"] = newGetRentBean!!.package_id
            params["price"] = "${newGetRentBean!!.price}"
            val options: ArrayList<NewGetRentBean.Data.Option> = ArrayList();
            if (selectOption != null && selectOption!!.id != "") {
                options.add(selectOption!!)
            }
            newGetRentBean!!.options.forEach {
                if (tv_select_charge.text.toString() != "否" && it.option_type == "5") {
                    options.add(it)
                }
                if (tv_select_platform.text.toString() != "否" && it.option_type == "4") {
                    options.add(it)
                }
                if (tv_select_insurance.text.toString() != "否" && it.option_type == "3") {
                    if (tv_select_insurance.text.toString().startsWith(it.name)) {
                        options.add(it)
                    }
                }
            }
            params["options"] =  options
        }
        return params
    }
}
