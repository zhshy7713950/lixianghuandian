package com.ruimeng.things.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.GetDepositBean
import com.ruimeng.things.home.bean.GetPayByDepositBean
import com.ruimeng.things.home.vm.DepositViewModel
import com.ruimeng.things.me.contract.FgtContractSignStep1
import com.utils.OptionPickerUtil
import com.utils.TextUtil
import com.utils.ToastHelper
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_deposit.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.*
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.http

/**api
 * Created by wongxd on 2018/11/13.
 * 账户押金
 */
class FgtDeposit : BaseBackFragment() {

    companion object {

        enum class PayWay(des: String) {
            NULL(""), //不选择任何方式
            WX(""), //微信
            AL(""), //alipay
            ZM(""), //芝麻信用
            BT(""),//白条
            FQ(""),//分期
            XX(""), //线下
            GROUPPAP(""), //集团支付
        }
        var deviceId =""
        var deviceModel =""

        fun newInstance(deviceId: String, isHost: String): FgtDeposit {
            return FgtDeposit().apply {
                arguments = Bundle().apply {
                    putString("deviceId", deviceId)
                    putString("isHost", isHost)
                }
            }
        }
    }

    val deviceId: String by lazy { arguments?.getString("deviceId") ?: "" }
    val getIsHost: String by lazy { arguments?.getString("isHost") ?: "" }
    private val vm: DepositViewModel by viewModels()

    override fun getLayoutRes(): Int = R.layout.fgt_deposit


    private var IS_CHECKED_PROTOCOL = false
    private var agentMap : JSONObject? = null;


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "支付押金")

        WaitViewController.from(scroll_account_deposit) { renderChilds() }

        Log.i("data===", "===getIsHost===$getIsHost")

        getDeposit()
//        getAgentList()

        EventBus.getDefault().register(this)
    }

    @Subscribe
    fun checkContract(event: ContractCheckEvent) {
        IS_CHECKED_PROTOCOL = true
        setCheckStatus()
    }



    private val currentDepositOption: MutableList<GetDepositBean.Data.DepositOption> =
        mutableListOf()

    @SuppressLint("SetTextI18n")
    private fun initAfterData(data: GetDepositBean.Data) {

        WaitViewController.from(scroll_account_deposit) { removeChilds() }
        FgtDeposit.deviceId = deviceId
        FgtDeposit.deviceModel = data.device.device_model

        tv_battery_num_account_deposit.text = "" + data.device.device_id
        et_agnet_name_deposit_code.setText(FgtHome.AGENT_CODE)

        tv_battery_model_account_deposit.text = "" + data.device.device_model


//        dealBtnYesAndNo(FgtHome.IsWholeBikeRent)
        tv_account_deposit.setOnClickListener {
            var data = arrayOf("是", "否").toMutableList();
            OptionPickerUtil.showOptionPicker(activity, data, object :
                OnOptionsSelectListener {
                override fun onOptionsSelect(
                    options1: Int,
                    options2: Int,
                    options3: Int,
                    v: View?
                ) {
                    tv_account_deposit.text = data.get(options1)
                    FgtHome.IsWholeBikeRent = options1 == 0
                    showTotalMoney()
                }
            })
        }
        if (data.deposit_option.size > 0) {
            tv_select_package_deposit.text = data.deposit_option.get(0).name
            batteryCombinationBean = data.deposit_option.get(0)
            showTotalMoney()
            tv_select_package_deposit.setOnClickListener {
                var filters =
                    data.deposit_option.map { depositOption: GetDepositBean.Data.DepositOption -> depositOption.name }
                OptionPickerUtil.showOptionPicker(activity, filters, object :
                    OnOptionsSelectListener {
                    override fun onOptionsSelect(
                        options1: Int,
                        options2: Int,
                        options3: Int,
                        v: View?
                    ) {
                        tv_select_package_deposit.text = filters.get(options1)
                        batteryCombinationBean = data.deposit_option.get(options1)
                        showTotalMoney()
                    }
                })
            }
        }

        btn_pay_now_account_deposit.setOnClickListener {
            if (!IS_CHECKED_PROTOCOL) {
                EasyToast.DEFAULT.show("请先同意租赁协议")
                return@setOnClickListener
            }

            if (PAY_WAY_TAG == PayWay.NULL) {
                EasyToast.DEFAULT.show("请选择支付方式")
                return@setOnClickListener
            }
//            if (tv_account_deposit.text.toString() == "请选择"){
//                EasyToast.DEFAULT.show("请选择是否整租车架")
//                return@setOnClickListener
//            }

            showProgressDialog("请求支付信息中")
            vm.getMyDevice().observe(this, Observer {
                if(it.isNotEmpty()){
                    dissmissProgressDialog()
                    if(it.size == 1){
                        NormalDialog(activity)
                            .apply {
                                style(NormalDialog.STYLE_TWO)
                                btnNum(2)
                                title("提示")
                                content("您已有1块待用电池，请问是否继续支付")
                                btnText("继续支付", "取消")
                                setOnBtnClickL(OnBtnClickL {
                                    dismiss()
                                    showProgressDialog("请求支付信息中")
                                    getPayByDeposit()
                                }, OnBtnClickL {
                                    dismiss()
                                })
                            }.show()
                    }else{
                        NormalDialog(activity)
                            .apply {
                                style(NormalDialog.STYLE_ONE)
                                btnNum(1)
                                title("提示")
                                content("您已有至少2块待用电池，可租用电池数已达上限")
                                btnText("确认")
                                setOnBtnClickL(OnBtnClickL {
                                    dismiss()
                                })
                            }.show()
                    }
                }else{
                    getPayByDeposit()
                }
            })

        }

        iv_check.setOnClickListener {
            IS_CHECKED_PROTOCOL = !IS_CHECKED_PROTOCOL
            setCheckStatus()
        }

        tv_view_rant_protocol_account_ya.setOnClickListener {
//            val dlg = DialogFragmentRentProtocol()
//            dlg.show(childFragmentManager, "protocol")
            start(
                FgtContractSignStep1.newInstance("", "", 0, 1,deviceId,data.device.device_model)
            )
        }

        et_agnet_name_deposit_code.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (agentMap != null && agentMap!!.has(
                        et_agnet_name_deposit_code.text.toString().trim()
                    )
                ) {
                    et_agnet_name_deposit.text =
                        agentMap!!.getString(et_agnet_name_deposit_code.text.toString().trim())
                } else {
                    et_agnet_name_deposit.text = ""
                }
            }
        }
        )
        dealPayWay()
    }
    private fun setCheckStatus(){
        if (!IS_CHECKED_PROTOCOL) {
            iv_check.setImageResource(R.mipmap.ic_radio_unselect)
        } else {
            iv_check.setImageResource(R.mipmap.ic_radio_select)
        }
    }

    private  fun showTotalMoney(){

            if (PAY_WAY_TAG == PayWay.ZM){
                showZMInfo()
            }else{
                tv_zm_desc1.visibility = View.GONE
                tv_zm_desc.visibility = View.GONE
                btn_pay_now_account_deposit.text = "立即支付"
                batteryCombinationBean.let {
                    if (it != null) {
                        tv_money_account_deposit.text = TextUtil.getMoneyText(if (FgtHome.IsWholeBikeRent) it.deposit_host else it.deposit)
                        tv_total.text = tv_money_account_deposit.text
                    }
                }
            }


    }

    private fun showZMInfo(){
        tv_money_account_deposit.text = TextUtil.getMoneyText("0.00")
        tv_zm_desc1.visibility = View.VISIBLE
        tv_zm_desc.visibility = View.VISIBLE
        tv_total.text = tv_money_account_deposit.text
        btn_pay_now_account_deposit.text = "申请免押"
    }
    private var PAY_WAY_TAG = PayWay.WX

    private fun dealPayWay() {
        rgDeposit.setOnCheckedChangeListener { group, id ->
            when (id) {
                R.id.rbWx -> PAY_WAY_TAG =PayWay.WX
                R.id.rbAlipay -> PAY_WAY_TAG = PayWay.AL
                R.id.rbOffline -> PAY_WAY_TAG = PayWay.ZM
            }
            showTotalMoney()
        }
    }
    private fun getAgentList(){
        http{
            url = "/apiv6/payment/getagentlist"
            params["name"] = ""
            onSuccess { res ->
                Log.i("TAG", "getAgentList: "+res)
                val result = JSONObject(res)
                if (result.getInt("errcode") == 200){
                    agentMap = result.getJSONObject("data")
                    if (deviceId.startsWith("8") && deviceId.length == 8){
                        if (agentMap != null && agentMap!!.has(FgtHome.AGENT_CODE)) {
                            et_agnet_name_deposit_code.setText(FgtHome.AGENT_CODE)
                            et_agnet_name_deposit.text = agentMap!!.getString(FgtHome.AGENT_CODE)
                        } else {
                            et_agnet_name_deposit.text = ""
                        }
                    }
                }
            }
        }

    }


    /**
     * 获取押金信息
     */
    private fun getDeposit() {

        http {
            url = "apiv4/getdeposit"
            params["device_id"] = deviceId
            params["is_host"] = "2"
            params["cg_mode"] = "1"
            onSuccess { res ->

                scroll_account_deposit?.let {
                    val result = res.toPOJO<GetDepositBean>().data
                    initAfterData(result)
                }
            }

            onFail { i, s ->
                if (i == 3001) {
                    //如果errcode=3001 直接跳转到 押金支付界面
                    startWithPop(FgtPayRentMoney.newInstance(deviceId))
                }
            }
        }
    }


    private var dlgPayProgress: SweetAlertDialog? = null

    private var dlgPaySuccessed: SweetAlertDialog? = null

    private var dlgPayFailed: SweetAlertDialog? = null

    private var batteryPackageBean: GetDepositBean.Data.Package? = null

    private var batteryCombinationBean: GetDepositBean.Data.DepositOption? = null

    private fun getPayByDeposit() {

        val agentCode = et_agnet_name_deposit_code.text.toString()

        if (agentCode.isBlank()) {
            dissmissProgressDialog()
            EasyToast.DEFAULT.show("请输入经销商代码")
            return
        }



        http {
            url = "apiv5/getpaybydeposit"
            params["device_id"] = deviceId
            params["code"] = agentCode
            params["cg_mode"] = "1"
            params["appType"] = "lxhd"
            params["host"] =  "2"
            //1微信支付2支付宝支付99线下支付 芝麻信用 101
            params["pay_type"] = when (PAY_WAY_TAG) {
                PayWay.WX -> "1"
                PayWay.AL -> "2"
                PayWay.ZM -> "101"
                PayWay.BT -> "3"
                PayWay.GROUPPAP -> "102"
                else -> "99"
            }
            params["package_id"] =  "0"
            params["opt_id"] = batteryCombinationBean?.id?.toString() ?: ""

            onSuccessWithMsg { res, msg ->
                val result = res.toPOJO<GetPayByDepositBean>().data


                dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
                dlgPaySuccessed = getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") {
                    //押金支付成功  跳转租金支付界面
//                    startWithPop(FgtPayRentMoney.newInstance(deviceId))
                    pop()
                    FgtHome.tryToScan(prefix = AtyScanQrcode.TYPE_PAY_RENT)
                    EventBus.getDefault().post(FgtHome.RefreshMyDeviceList())
                }
                dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")

                dlgPayProgress?.show()


                if (PAY_WAY_TAG == PayWay.WX) {
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
                            getServerPayResult(result.orderid, true)
                        }

                        override fun onFail(msg: String?) {
                            getServerPayResult(result.orderid, false)
                        }
                    })
                } else if (PAY_WAY_TAG == PayWay.AL || PAY_WAY_TAG == PayWay.ZM) {

                    BaseAlipay.tryPay(result.alipay.paystr) { resultInfo, resultStatus, isLocalSuccessed ->
                        getServerPayResult(result.orderid, isLocalSuccessed)
                    }

                } else if (PAY_WAY_TAG == PayWay.BT) {
                    dlgPayProgress?.dismiss()
                    dlgPaySuccessed?.show()
                } else if (PAY_WAY_TAG == PayWay.GROUPPAP) {
                    dlgPayProgress?.dismiss()
                    dlgPaySuccessed?.show()
                } else {
                    dlgPayProgress?.dismiss()
                    dlgPaySuccessed?.show()
                }


                btn_pay_now_account_deposit.postDelayed({
                    if (dlgPayProgress?.isShowing ?: false) {
                        dlgPayProgress?.dismissWithAnimation()
                    }
                }, 10 * 1000)

            }

            onFinish {
                dissmissProgressDialog()
            }
        }
    }


    private var retryTime = 0

    /**
     * 获取服务器上的支付结果
     */
    private fun getServerPayResult(orderId: String, shouldRetry: Boolean) {

        fun dealShouldRetry() {
            if (retryTime <= 3 && shouldRetry) {
                btn_pay_now_account_deposit?.postDelayed({
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
}