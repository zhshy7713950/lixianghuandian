package com.ruimeng.things.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigkoo.pickerview.view.OptionsPickerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.FgtMain
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.Path
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.home.adapter.BasePackageAdapter
import com.ruimeng.things.home.adapter.ChangePackageAdapter
import com.ruimeng.things.home.bean.CountAmountBean
import com.ruimeng.things.home.bean.GetRentPayBean
import com.ruimeng.things.home.bean.NewGetRentBean
import com.ruimeng.things.home.bean.PaymentInfo
import com.ruimeng.things.home.bean.PaymentOption
import com.ruimeng.things.home.bean.UpdateGetRentBean
import com.ruimeng.things.home.view.CompanyDescPopup
import com.ruimeng.things.home.view.SelectCouponPopup
import com.ruimeng.things.me.bean.MyCouponBean
import com.ruimeng.things.me.contract.FgtContractSignStep1
import com.ruimeng.things.me.contract.FgtMyContractDetail
import com.ruimeng.things.me.credit.FgtCreditSystem
import com.utils.OptionPickerUtil
import com.utils.TextUtil
import com.utils.ToastHelper
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_pay_rent_money.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.textColor
import org.json.JSONObject
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http
import java.lang.Exception
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date


/**
 * Created by wongxd on 2018/11/13.
 */
class FgtPayRentMoney : BaseBackFragment() {

    data class EventInstallmentPaymentSuccess(val isNeedPop: Boolean = false)

    companion object {
        public val PAGE_TYPE_CREATE = 0
        public val PAGE_TYPE_UPDATE = 1
        fun newInstance(deviceId: String, type: Int = PAGE_TYPE_CREATE): FgtPayRentMoney {
            val fgt = FgtPayRentMoney()
            val b = Bundle()
            b.putString("deviceId", deviceId)
            b.putInt("pageType", type)
            fgt.arguments = b
            return fgt
        }
    }

    val deviceId: String by lazy { arguments?.getString("deviceId") ?: "" }
    val pageType: Int by lazy {
        arguments?.getInt("pageType") ?: PAGE_TYPE_CREATE
    } //页面类型 0 创建租金，1 续费升级
    private val basePackageAdapter: BasePackageAdapter by lazy { BasePackageAdapter() }
    private val changePackageAdapter: ChangePackageAdapter by lazy { ChangePackageAdapter() }
    override fun getLayoutRes(): Int = R.layout.fgt_pay_rent_money
    private var IS_CHECKED_PROTOCOL = false
    private var newGetRentBean: PaymentInfo? = null
    private var selectOption: PaymentOption? = null
    private var baseInfo: PaymentInfo? = null
    private var sighStatus = false

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        EventBus.getDefault().register(this)
        initTopbar(topbar, if (pageType == PAGE_TYPE_CREATE) "购买套餐" else "续期升级")
        getRent()
        dealPayWay()
        initView()
        initTicket()
    }

    private fun initView() {
        val colors = arrayOf("#FFFFFF", "#929FAB")
        tv_change_package_create_title.text =
            TextUtil.getSpannableString(arrayOf("换电套餐", ""), colors)
        tv_base_package_create_title.text = "租电套餐"

        if (pageType == PAGE_TYPE_CREATE) {
            ll_package.visibility = View.GONE
            ll_change_package_create_title.visibility = View.VISIBLE
            tv_change_package_update_title.visibility = View.GONE
            ll_expand.visibility = View.GONE
            tv_base_package_create_title.visibility = View.VISIBLE
            cl_update_package_title.visibility = View.GONE
        } else {
            ll_package.visibility = View.GONE
            tv_base_package_update_title.text = "可选续期套餐"
            ll_change_package_create_title.visibility = View.GONE
            tv_change_package_update_title.visibility = View.VISIBLE
            ll_expand.visibility = View.VISIBLE
            tv_base_package_create_title.visibility = View.GONE
            tv_rent_desc.visibility = View.GONE
            cl_update_package_title.visibility = View.VISIBLE
        }
        tv_company_desc.setOnClickListener {
            var text = "1.换电服务的使用资费\n" +
                    "2.请在【租电套餐】选择完成后，再继续选择对应的换电服务\n" +
                    "3.本套餐取电起租后，不支持退租金\n" +
                    "注：【换电套餐】的有效期限取决于【租电套餐】。若【租电套餐】失效，那么【换电套餐】也会无法继续使用"
            activity?.let { it1 -> CompanyDescPopup(it1,"换电套餐说明",text) }
        }
        tv_option_desc.setOnClickListener {
            var text = "1.【租电套餐】选择完毕后，”带充电器“选项将会显示出对应的押金金额\n" +
                    "2.同时，”租赁车架“选项，将会依据所选【租电套餐】的月数时长，计算相应所需支付金额"
            if (pageType == PAGE_TYPE_UPDATE){
                text = "1.若在【已购套餐】中，已经支付过”充电器“的押金，则后续无需再次支付\n" +
                        "2.若在【已购套餐】中，已经选择过”租赁车架“，则续期升级时，无法进行更改对应选项，将会延续选择如下：\n" +
                        "1）选择其他【租电套餐】，”租赁车架“选项将会依据所选【租电套餐】的月数时长，计算相应所需支付金额"
            }
            activity?.let { it1 -> CompanyDescPopup(it1,"附加选项说明",text) }
        }
        tv_rent_desc.setOnClickListener {
            var text = "1.租用电池的基础套餐费用\n" +
                    "换电操作不会引起租电套餐费用变更\n" +
                    "2.本套餐取电起租后，不支持退租金"
            activity?.let { it1 -> CompanyDescPopup(it1,"附加选项说明",text) }
        }
        tv_option_time.text = showExpireTitle() + "无"
        tv_rant_long_pay_time.text = showExpireTitle() + "无"
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

    private fun dealPayWay() {
        rgPayRent.setOnCheckedChangeListener { group, id ->
            when (id) {
                R.id.rbWx -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.WX
                R.id.rbAlipay -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.AL
                R.id.rbOffline -> PAY_WAY_TAG = FgtDeposit.Companion.PayWay.XX
            }
        }
    }

    private fun resetSelectOptionList() {
        var optionList: ArrayList<PaymentOption> = ArrayList()
//        val paymentName = if (pageType == PAGE_TYPE_CREATE) "暂不购买" else "暂不续期"
//        optionList.add(PaymentOption(name = paymentName))
        newGetRentBean?.let {
            optionList.addAll(newGetRentBean!!.options.filter { it.option_type == "2" })
            tv_rant_long_pay_time.text =
                showExpireTitle() + TextUtil.formatTime(it.show_start_time, it.show_end_time)
            tv_option_time.text = tv_rant_long_pay_time.text
        }
        changePackageAdapter.selectPos = 0
        changePackageAdapter.setNewData(optionList)
//        tv_option_time.text = showExpireTitle() + "无"
        selectOption = null
        setSelectOption()
        computeAmount()
    }

    private fun initViewAfterData(list: List<PaymentInfo>) {
        baseInfo.let {
            tv_battery_num_pay_rent_money.text = it!!.device_id
            tv_battery_model_pay_rent_money.text = it!!.model_name
            tv_agnet_name_pay_rent_money.text = it!!.agentCode
            et_agnet_name_deposit.text = it!!.agentName
            btn_return_deposit_pay_rent_money.visibility =
                if (it!!.btn_return == 1) View.GONE else View.GONE
            btn_return_deposit_pay_rent_money.setOnClickListener {
                tryReturnDeposit(baseInfo!!.contract_id)
            }
            getSignStatus(it.contract_id)
        }
        if (list.isEmpty()) {
            ToastHelper.shortToast(context, "没有找到套餐")
            return
        }
        newGetRentBean = list.get(0)

        basePackageAdapter.setNewData(list)
        resetSelectOptionList()
        rv_rant_long_pay_rent_money.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rv_rant_long_pay_rent_money.adapter = basePackageAdapter

        basePackageAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener {
            override fun onItemClick(p0: BaseQuickAdapter<*, *>?, p1: View?, p2: Int) {
                newGetRentBean = list.get(p2)
                basePackageAdapter.selectPos = p2
                basePackageAdapter.notifyDataSetChanged()
                resetSelectOptionList()
            }
        })
        rv_change_package.layoutManager = GridLayoutManager(activity, 2)
        rv_change_package.adapter = changePackageAdapter
        changePackageAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener {
            override fun onItemClick(p0: BaseQuickAdapter<*, *>?, p1: View?, p2: Int) {
                selectOption = changePackageAdapter.data.get(p2)
                changePackageAdapter.selectPos = p2
                changePackageAdapter.notifyDataSetChanged()
                computeAmount()
//                selectOption.let {
//                    if (it != null) {
//                        tv_option_time.text = showExpireTitle() + TextUtil.formatTime(
//                            it.show_start_time,
//                            it.show_end_time
//                        )
//                    }
//                }

            }
        })
        setSelectOption()
        computeAmount()

        btn_pay_now_pay_rent_money.setOnClickListener { view ->
            if (!IS_CHECKED_PROTOCOL) {
                EasyToast.DEFAULT.show("请先同意协议")
                return@setOnClickListener
            }
            if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.NULL) {
                EasyToast.DEFAULT.show("请选择支付方式")
                return@setOnClickListener
            }
            computeAmount(true)
        }


        iv_check_pay_rent_money.setOnClickListener {
            if (pageType == PAGE_TYPE_CREATE){
                if (sighStatus){
                    IS_CHECKED_PROTOCOL = true
                    iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_select)
                }else{
                    baseInfo.let {
                        if (it != null) {
                            start(
                                FgtContractSignStep1.newInstance(baseInfo!!.contract_id, "", 0, if (pageType == PAGE_TYPE_CREATE) 2 else 1,deviceId,baseInfo!!.model_name)
                            )
                        }
                    }
                }
            }else{
                if (IS_CHECKED_PROTOCOL) {
                    iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_unselect)
                } else {
                    iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_select)
                }

                IS_CHECKED_PROTOCOL = !IS_CHECKED_PROTOCOL
            }

        }


        tv_view_rant_protocol_pay_rent_money.setOnClickListener {
//            val dlg = DialogFragmentRentProtocol()
//            dlg.show(childFragmentManager, "protocol")
            if (sighStatus){
                baseInfo.let {
                    if (it != null) {
                        start(FgtMyContractDetail.newInstance(it.contract_id,it.device_id))
                    }
                }
            }else{
                start(
                    FgtContractSignStep1.newInstance(baseInfo!!.contract_id, "", 0, if (pageType == PAGE_TYPE_CREATE) 2 else 1,deviceId,baseInfo!!.model_name)
                )
            }

        }


        tv_ticket_pay_rent_money.setOnClickListener {
            if (!couponList.isEmpty()) {
                activity?.let { it1 ->
                    SelectCouponPopup(
                        it1,
                        couponList,
                        couponId,
                        object : BaseQuickAdapter.OnItemClickListener {
                            override fun onItemClick(
                                p0: BaseQuickAdapter<*, *>?,
                                p1: View?,
                                p2: Int
                            ) {
                                val item = couponList[p2]
                                if (couponId == item.id) {
                                    tv_ticket_pay_rent_money.text = "不使用优惠券"
                                    couponId = 0
                                } else {
                                    tv_ticket_pay_rent_money.text = item.coupon_label
                                    couponId = item.id
                                }
                                computeAmount()
                            }
                        })
                }
            }

//            pvOptions?.show()
//            activity?.window?.let { it1 -> couponPopup?.show(it1.decorView) }
        }

    }


    private fun showOptionSelector(paymentInfo: PaymentInfo,type:String,textView: TextView,clickView: View,titleText:TextView){
        when (type) {
            "5" -> titleText.text = "是否带充电器"
            "4" -> titleText.text = "是否租赁车架"
            "3" -> titleText.text = "是否购买保险"
        }
        textView.textColor = Color.parseColor("#929FAB")
        clickView.setOnClickListener {}
        if (paymentInfo.pname == "暂不续期"){
            textView.text = "无需选择"
        }else{
            val filterOptions = paymentInfo.options.filter { it.option_type == type }
            if (filterOptions != null && filterOptions.size > 0){
                when (type){
                    "5"->titleText.text = TextUtil.getDoubleSizeText("是否带充电器","(押金${filterOptions[0].price}元)",0.9f)
                    "4"->titleText.text =TextUtil.getDoubleSizeText("是否租赁车架","(${filterOptions[0].price}元/月)",0.9f)
                    "3"->titleText.text = "是否购买保险"
                }

                var alreadyBuy = false
                if (pageType == PAGE_TYPE_UPDATE){
                    if (type == "5" && tv_other_option1.text.toString() !="否"){
                        alreadyBuy = true
                    }else if (type == "4" && tv_other_option2.text.toString() !="否"){
                        alreadyBuy = true
                    }
                }
                if (alreadyBuy){
                    val price = BigDecimal(filterOptions.get(0).price).multiply(BigDecimal(paymentInfo.time_num))
                    textView.text = "是(${ price.toDouble() }元)"
                    textView.textColor = Color.parseColor("#929FAB")
                }else{
                    textView.textColor = Color.WHITE
                    textView.text = "请选择"
                }
                clickView.setOnClickListener {
                    if (alreadyBuy){
                        if (type == "5"){
                            ToastHelper.shortToast(context,"”已购套餐“中已支付过充电器押金，无需再次支付")
                        }else{
                            ToastHelper.shortToast(context,"”已购套餐“中已经选择过租赁车架“，无法进行更改")
                        }
                    }else{
                        var filters: ArrayList<String> = ArrayList()
                        filters.add("否")
                        if (type == "3"){
                            filterOptions.forEach {
                                filters.add("${it.name }(${it.price}元)")
                            }
                        }else if (type == "5"){
                            filters.add("是(${ filterOptions.get(0).price }元)")
                        } else{
                            val price = BigDecimal(filterOptions.get(0).price).multiply(BigDecimal(paymentInfo.time_num))
                            filters.add("是(${ price.toDouble() }元)")
                        }
                        OptionPickerUtil.showSingleOptionPicker(activity,filters){
                            textView.text = it
                            computeAmount()
                        }
                    }

                }
            }else {
                textView.text = "否"
            }
        }
        if (type == "5" && tv_other_option1.text.toString() !="否" && pageType == PAGE_TYPE_UPDATE){
            textView.text = "押金已付"
            textView.textColor = Color.parseColor("#929FAB")
        }
    }
    private fun setSelectOption(){
        newGetRentBean.let {
            if (it != null) {
                showOptionSelector(it,"5",tv_select_charge,cl_select_charge,tv_select_charge_title)
                showOptionSelector(it,"4",tv_select_platform,cl_select_platform,tv_select_platform_title)
                showOptionSelector(it,"3",tv_select_insurance,cl_select_insurance,tv_select_insurance_title)
            }
        }
    }


    private fun showExpireTitle(): String {
        val expire = if (pageType == PAGE_TYPE_CREATE) "有效期：" else "  有效期："
        return expire
    }

    @Subscribe
    fun checkContract(event: ContractCheckEvent) {
        IS_CHECKED_PROTOCOL = true
        sighStatus = true
        iv_check_pay_rent_money.setImageResource(R.mipmap.ic_radio_select)
        tv_view_rant_protocol_pay_rent_money.setOnClickListener {
            baseInfo.let {
                if (it != null) {
                    start(FgtMyContractDetail.newInstance(it.contract_id,it.device_id))
                }
            }

        }
    }

    private var dlgPayProgress: SweetAlertDialog? = null

    private var dlgPaySuccessed: SweetAlertDialog? = null

    private var dlgPayFailed: SweetAlertDialog? = null


    private var retryTime = 0
    val couponList = mutableListOf<MyCouponBean.Data>()

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
                if (result.isEmpty()) {
                    tv_ticket_pay_rent_money.text = "暂无可用优惠券"
                } else {
                    tv_ticket_pay_rent_money.text = "请选择"
                }
                couponList.addAll(result)
            }


            onFail { i, s ->
                IS_TICKET_DATA_INIT = false
            }

        }
    }

    private var PAY_WAY_TAG = FgtDeposit.Companion.PayWay.WX


    /**
     * 获取设备租用信息
     */
    private fun getRent() {
        if (pageType == PAGE_TYPE_CREATE) {
            http {
                url = PathV3.GET_RENT
                params["deviceId"] = deviceId
                onSuccessWithMsg { res, msg ->
                    iv_battery_pay_rent_money?.let {
                        val result = res.toPOJO<NewGetRentBean>().data
                        if (!result.isEmpty()) {
                            baseInfo = result.get(0)
                            initViewAfterData(result)
                        }
                    }
                }
                onFail { i, s ->
                    iv_battery_pay_rent_money.postDelayed({
                        pop()
                    }, 1000)
                }
            }
        } else {
            http {
                url = "/apiv6/payment/upgrade"
                params["user_id"] = "${InfoViewModel.getDefault().userInfo.value?.id}"
                params["device_id"] = deviceId
                onSuccessWithMsg { res, msg ->
                    iv_battery_pay_rent_money?.let {
                        val result = res.toPOJO<UpdateGetRentBean>().data
                        var payments = ArrayList<PaymentInfo>();
                        payments.add(PaymentInfo(pname = "暂不续期", options = result.options))
                        payments.addAll(result.paymentInfo)
                        baseInfo = result.baseInfo
                        baseInfo!!.model_name = baseInfo!!.modelName
                        showBaseInfo(result)
                        initViewAfterData(payments)

                    }
                }
            }
        }
    }

    private fun showBaseInfo(data: UpdateGetRentBean.Data) {

        try {


            tv_base_package_name.text = data.baseInfo.paymentName
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                tv_base_package_time.text =
                    sdf.format(Date(data.baseInfo.begin_time.toLong() * 1000)) + "至" + sdf.format(
                        Date(data.baseInfo.exp_time.toLong() * 1000)
                    )
            } catch (e: Exception) {
                tv_base_package_time.text = "暂无"
            }
            val userOptions = data.userOptions
            tv_other_option1.text = "${
                if (userOptions.count { it.option_type == "5" } > 0) "是(${
                    userOptions.filter { it.option_type == "5" }.first().price
                }元)" else "否"
            }"
            tv_other_option2.text = "${
                if (userOptions.count { it.option_type == "4" } > 0) "是(${
                    userOptions.filter { it.option_type == "4" }.first().price
                }元)" else "否"
            }"
            if (userOptions.count { it.option_type == "3" } > 0){
                val option = userOptions.filter { it.option_type == "3" }.first()
                val price = BigDecimal(option.active_time) .multiply(BigDecimal(option.price))
                tv_other_option3.text ="保险${option.active_time}个月(${price.toDouble()}元)"
            }else{
                tv_other_option3.text ="否"
            }
            val changeOptions = userOptions.filter { it.option_type == "2" }
            ll_change_package_no_active.visibility = View.GONE
            ll_change_package_active.visibility = View.GONE
            tv_change_package_title.visibility = View.GONE
            if (!changeOptions.isEmpty()) {
                tv_change_package_title.visibility = View.VISIBLE
                changeOptions.forEach {
                    if (it.active_status == "1") {
                        ll_change_package_active.visibility = View.VISIBLE
                        tv_change_package_name1.text = "次数无限制"
                        tv_change_package_time1.text = tv_base_package_time.text
                    } else if (it.active_status == "2") {
                        ll_change_package_no_active.visibility = View.VISIBLE
                        tv_change_package_name2.text = "换电${it.total_times}次"
                        tv_change_package_time2.text = tv_base_package_time.text
                    }
                }
            }
            tv_expand.text = "展开"
            ll_package.visibility = View.GONE
            tv_expand.setOnClickListener {
                if (tv_expand.text.equals("展开")) {
                    ll_package.visibility = View.VISIBLE
                    tv_expand.text = "收起"
                } else {
                    ll_package.visibility = View.GONE
                    tv_expand.text = "展开"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG", "showBaseInfo: " + e.message)
        }
    }

    private fun computeAmount(submit: Boolean = false) {
        http {
            url = "/apiv6/payment/computeamount"
            jsonParam = getSubmitParam()
            onSuccessWithMsg { res, msg ->
                val result = res.toPOJO<CountAmountBean>().data
                tv_total_price.text = TextUtil.getMoneyText("${DecimalFormat("#.##").format(result.totalPrice)}")
                tv_coupon_price.text = "已优惠¥${ DecimalFormat("#.##").format(result.couponAmount)}"
                if (submit) {
                    countPay()
                }
            }

        }
    }

    private fun countPay() {
        dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
        dlgPaySuccessed =
            getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") { paySuccessed() }
        dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")
        dlgPayProgress?.show()
        http {
            url =
                if (pageType == PAGE_TYPE_CREATE) "apiv6/payment/payrentmoney" else "/apiv6/payment/upgradepay"
            jsonParam = getSubmitParam()
            //支付方式 1微信支付2支付宝支付3白条4免息支付99线下现金100套餐订单101支付宝预授权
            jsonParam["payType"] = if (PAY_WAY_TAG == FgtDeposit.Companion.PayWay.WX) "1"
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


    private fun getSubmitParam(): MutableMap<String, Any> {
        var params: MutableMap<String, Any> = mutableMapOf()
        params["deviceId"] = deviceId
        params["user_id"] = "${InfoViewModel.getDefault().userInfo.value?.id}"
        params["appType"] = "lxhd"
        if (couponId != 0) {
            params["couponId"] = "${couponId}"
        }

        if (newGetRentBean != null) {
            if (newGetRentBean!!.id != "") {
                params["payment_id"] = newGetRentBean!!.id
                params["package_id"] = newGetRentBean!!.package_id
                params["price"] = "${newGetRentBean!!.price}"
            }
            val options: ArrayList<PaymentOption> = ArrayList();
            if (selectOption != null && selectOption!!.id != "") {
                options.add(selectOption!!)
            }
            newGetRentBean!!.options.forEach {
                if (tv_select_charge.text.toString().contains("元") && it.option_type == "5") {
                    options.add(it)
                }
                if (tv_select_platform.text.toString().contains("元") && it.option_type == "4") {
                    options.add(it)
                }
                if (tv_select_insurance.text.toString().contains("元") && it.option_type == "3") {
                    if (tv_select_insurance.text.toString().startsWith(it.name)) {
                        options.add(it)
                    }
                }
            }
            params["options"] = options
        }
        return params
    }

    private fun getSignStatus(contractId: String){
        http {
            url = PathV3.SIGN_CONTRACT
            params["contract_id"] = contractId
            IS_SHOW_MSG = false
            onSuccess {
                sighStatus = false

            }
            onFail { i, s ->
                if ( i == 202){
                    sighStatus = true

                }
            }
        }
    }
}
