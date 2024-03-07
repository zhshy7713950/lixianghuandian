package com.ruimeng.things.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.ruimeng.things.*
import com.ruimeng.things.home.bean.*
import com.ruimeng.things.home.view.BuyChangePackagePopup
import com.ruimeng.things.home.view.ChangePackageListPopup
import com.ruimeng.things.home.view.ShowCouponPopup
import com.ruimeng.things.me.FgtMeDeposit
import com.ruimeng.things.me.FgtTrueName
import com.ruimeng.things.me.contract.FgtContractSignStep1
import com.utils.*
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.activity_balance_withdrawal.*
import kotlinx.android.synthetic.main.fgt_deposit.*
import kotlinx.android.synthetic.main.fgt_home.*
import kotlinx.android.synthetic.main.fgt_me.tv_ya_money_me
import kotlinx.android.synthetic.main.fgt_pay_rent_money.tv_base_package_time
import kotlinx.android.synthetic.main.home_status_item.*
import kotlinx.android.synthetic.main.home_status_no_item.*
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.sp
import org.jetbrains.anko.textColor
import org.json.JSONObject
import wongxd.Config
import wongxd.base.MainTabFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.*
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.http
import wongxd.utils.utilcode.util.SPUtils


/**
 * Created by wongxd on 2018/11/9.
 */
class FgtHome : MainTabFragment() {

    companion object {
        const val REQUEST_ZXING_CODE = 1025
        const val KEY_LAST_DEVICE_ID = "lastDeviceId"

        var CURRENT_DEVICEID = ""
        var AGENT_CODE = ""
        var NO_PAY_DEVICEID = ""
        var CURRENT_CONTRACT_ID = ""
        var IS_OPEN = false

        var IsWholeBikeRent = true
        var getIsHost = "1"
        var deviceStatus = 0 //0 有押金 有租金 1，无押金，无租金  2 有押金 无租金 3 已过期
        var userId = ""
        var hasChangePackege = false //是否有换电套餐
        var modelName = ""
        var contractId = ""
        var deposit = 0.0
        var rent_day = ""
        var rent_time = ""
        var totalvoltage = ""
        var rsoc = ""
        var payType =""

        fun selectDeviceType() {

        }
        /**
         * 尝试扫描二维码
         */
        fun tryToScan(prefix: String = "", oldContractId: String = "", isHost: String = "") {
            val userInfo = InfoViewModel.getDefault().userInfo.value
            userInfo?.let {
                if (it.realname_auth == 1){
                    getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                        AtyScanQrcode.start(getCurrentAty(), prefix, oldContractId, isHost)
                    })
                }else{
                    showTipDialog(
                        getCurrentAppAty(),
                        msg = "您还没有实名认证",
                        click = {
                            FgtMain.instance?.start(FgtTrueName())
                        })
                }
            }

        }

//        /**
//         * 处理扫码后的信息
//         */
//        fun dealScanResult(deviceId: String?,type:Int = FgtPayRentMoney.PAGE_TYPE_CREATE) {

//            deviceId ?: return
//
//            http {
//                url = "apiv6/cabinet/newRent"
//                params["code"] = deviceId
//                params["user_id"] = userId
//                onSuccess {
//                    //{"errcode":200,"errmsg":"\u64cd\u4f5c\u6210\u529f","data":{"status":0}}
//                    //status int 状态， 0 等待支付押金 -》进入押金支付界面，1 押金已支付 进入租金支付界面
//                    val json = JSONObject(it)
//                    val data = json.optJSONObject("data")
//                    if (data.get("couponInfo") != null){
//                        ShowCouponPopup(getCurrentAppAty(),data.optJSONObject("couponInfo"),object :OnClickListener{
//                            override fun onClick(p0: View?) {
//                                val status = data.optInt("status")
//                                when (status) {
//                                    0 -> FgtMain.instance?.start(FgtDeposit.newInstance(deviceId, getIsHost))
//                                    1 -> FgtMain.instance?.start(FgtPayRentMoney.newInstance(deviceId,type))
//                                    2 -> {
//                                        EasyToast.DEFAULT.show(json.optString("errmsg"))
//                                        showTipDialog(
//                                            getCurrentAppAty(),
//                                            msg = json.optString("errmsg"),
//                                            click = {
//                                                FgtMain.instance?.start(FgtTrueName())
//                                            })
//                                    }
//
//                                    else -> {
//                                    }
//                                }
//                            }
//                        }).show(rootView)
//                    }
//
//
//                }
//            }
//        }

    }

    override fun getLayoutRes(): Int = R.layout.fgt_home

    fun startFgt(toFgt: SupportFragment) {
        (parentFragment as FgtMain).start(toFgt)
    }

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)

        if (!TextUtils.isEmpty(Config.getDefault().spUtils.getString("ledString", ""))) {
            ledTextView?.text =  Config.getDefault().spUtils.getString("ledString", "")
            ledTextView?.init(activity?.windowManager)
            ledTextView?.startScroll()
        }

        right?.setOnClickListener {   tryToScan() }

        tv_follow_wechat.setOnClickListener { start(FgtFollowWechatAccount()) }
        tv_right.setOnClickListener {
            srl_home?.autoRefresh()
        }


        val userInfo = InfoViewModel.getDefault().userInfo.value
        userInfo?.let {
            if (userInfo.mobile.isBlank() && userInfo.mobile_bind != "1" && !SPUtils.getInstance().getBoolean("MOBILE_BIND_SKIP")) {
                val intent = Intent(activity,AtyLogin::class.java)
                intent.putExtra("pageType",1)
                activity?.startActivity(intent)
            }
            userId = userInfo.id
            tv_follow_wechat.visibility = if (userInfo.mp_follow == 0) View.VISIBLE else View.GONE
            tvUnbind.visibility = if (userInfo.is_debug == 1) View.VISIBLE else View.GONE
            tv_title.text = userInfo.nickname
        }


        CURRENT_DEVICEID = Config.getDefault().spUtils.getString(KEY_LAST_DEVICE_ID)

        dealTwoStatus(false)

        WaitViewController.from(root_has_item) { renderChilds() }

        srl_home?.setEnableLoadMore(false)
        srl_home.setOnRefreshListener { getBatteryDetailInfo(if (CURRENT_DEVICEID.isBlank()) "0" else CURRENT_DEVICEID) }

        srl_home.autoRefresh()

        initTabLayout()


    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden){
            srl_home.autoRefresh()
        }
    }
    /**
     * 处理扫码后的信息
     */
    @Subscribe
    public fun dealScanResult( event: ScanResultEvent) {
        var deviceId = event.deviceId

        http {
            url = "apiv6/cabinet/getbatterytype"
            params["code"] = event.deviceId
            onSuccess {
                val json = JSONObject(it)
                val data = json.optJSONObject("data")

                OptionPickerUtil.showJsonOptionPicker(activity, "请选择租用的电池型号",data, object :
                    OptionPickerUtil.OnSelectKey {
                    override fun selectKey(
                        key: String
                    ) {
                        newRent(event,key)
                    }
                })
            }
            onFail { i, s ->
                ToastHelper.shortToast(context,s)
            }
        }

    }

    fun newRent( event: ScanResultEvent,modelId:String){
        http {
            url = "apiv6/cabinet/newRent"
            params["code"] = event.deviceId
            params["user_id"] = userId
            params["modelId"] = modelId
            onSuccess {
                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                AGENT_CODE =  data.optString("code")
                if (data.optJSONObject("device") != null){
                    event.deviceId  = data.optJSONObject("device").optString("device_id")
//                    event.deviceId = CURRENT_DEVICEID
                }
                if (data.optJSONObject("couponInfo") != null){
                    ShowCouponPopup(getCurrentAppAty(),data.optJSONObject("couponInfo"),object :OnClickListener{
                        override fun onClick(p0: View?) {
                            doScanNext(json,event);
                        }
                    }).show(rootView)
                }else{
                    doScanNext(json,event);
                }
            }
        }
    }
    fun rentStep1( deviceId: String?,type:Int = FgtPayRentMoney.PAGE_TYPE_CREATE) {
        deviceId ?: return
        http {
            url = "apiv4/rentstep1"
            params["device_id"] = deviceId
            params["cg_mode"] = "1"
            onSuccess {
                //{"errcode":200,"errmsg":"\u64cd\u4f5c\u6210\u529f","data":{"status":0}}
                //status int 状态， 0 等待支付押金 -》进入押金支付界面，1 押金已支付 进入租金支付界面
                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val status = data.optInt("status")

                when (status) {
                    0 -> FgtMain.instance?.start(FgtDeposit.newInstance(deviceId, getIsHost))
                    1 -> FgtMain.instance?.start(FgtPayRentMoney.newInstance(deviceId,type))
                    2 -> {
                        EasyToast.DEFAULT.show(json.optString("errmsg"))
                        showTipDialog(
                            getCurrentAppAty(),
                            msg = json.optString("errmsg"),
                            click = {
                                FgtMain.instance?.start(FgtTrueName())
                            })
                    }

                    else -> {
                    }
                }
            }
        }
    }


    fun doScanNext(json : JSONObject, event: ScanResultEvent){
        val data = json.optJSONObject("data")
        val status = data.optInt("status")
        when (status) {
            0 -> FgtMain.instance?.start(FgtDeposit.newInstance(event.deviceId, getIsHost))
            1 -> FgtMain.instance?.start(FgtPayRentMoney.newInstance(event.deviceId,event.type))
            2 -> {
                EasyToast.DEFAULT.show(json.optString("errmsg"))
                showTipDialog(getCurrentAppAty(),msg = json.optString("errmsg"),click = {
                    FgtMain.instance?.start(FgtTrueName())
                })
            }
            else -> {
            }
        }
    }
    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }


    /**
     * 首页布局 有 已添加设备 和未加设备两种状态
     */
    private fun dealTwoStatus(isHasItem: Boolean) {
        root_has_item.visibility = View.GONE
        root_no_item.visibility = View.GONE

        if (isHasItem) {
            root_has_item.visibility = View.VISIBLE
            initHasItemView()
        } else {
            root_no_item.visibility = View.VISIBLE
            initNoItemView()
        }
    }

    private fun showHomePageInfo(){
        //0 有押金 有租金 1，无押金，无租金  2 有押金 无租金 3 已过期
        if (deviceCode == 201 && paymentCode == 208){
            deviceStatus = 1
        }else if (deviceCode == 201 && paymentCode == 200 ){
            if(paymentDetailBean!!.deposit_status == "0"){
                deviceStatus = 1
            }else{
                deviceStatus = 2
            }
        }
        else if (deviceCode == 200 && paymentCode == 200 && paymentDetailBean!!.active_status == "2"){
            deviceStatus = 3
        }
        else{
            deviceStatus = 0
        }
        if (deviceStatus == 0){
            dealTwoStatus(true)
            deviceDetailBean?.let { item ->
                showNewDeviceData(item)
                showPackageInfo()
            }
        }else{
            dealTwoStatus(false)
        }
    }

    private fun initNoItemView() {
       if (deviceStatus == 2){
            tv_log_info.text ="您还没有为电池（编号"+ NO_PAY_DEVICEID+"）购买套餐"
            tv_add_device.text  = "点击购买套餐"
            iv_add_device.visibility = View.GONE
           btnReturn.visibility = View.GONE
        }else if (deviceStatus == 3){

           tv_add_device.text  = "点击购买套餐"
           iv_add_device.visibility = View.GONE
           root_has_item.visibility = View.GONE
           root_no_item.visibility = View.VISIBLE
           btnReturn.visibility = View.VISIBLE
       } else{
            tv_add_device.text  = "点击添加"
           tv_log_info.text ="您还没有添加电池设备"
            iv_add_device.visibility = View.VISIBLE
           btnReturn.visibility = View.GONE

        }

        val llNoItem = root_no_item
        val addDeviceBtn = llNoItem.findViewById<FrameLayout>(R.id.addDeviceBtn)
        var event  = ScanResultEvent(NO_PAY_DEVICEID,FgtPayRentMoney.PAGE_TYPE_CREATE)
        addDeviceBtn.setOnClickListener {
            when (deviceStatus) {
                0-> {tryToScan()}
                1 -> {            ToastHelper.shortToast(context,"请扫描电柜二维码”")
                    tryToScan()}
                2 -> rentStep1(NO_PAY_DEVICEID)
                3-> rentStep1(NO_PAY_DEVICEID)
            }
        }
        btnReturn.setOnClickListener {
            startFgt(FgtReturn.newInstance(NO_PAY_DEVICEID))
        }

    }


    private fun initHasItemView() {

        tv_remark_num.setOnClickListener { changeRemark() }
        fl_change_remark_home.setOnClickListener { changeRemark() }

        tv_switch_battery.setOnClickListener { startFgt(FgtSwitchBattery()) }
        tvOpenClose.setOnClickListener {
            if (checkStatus()) {
                var title = if (tvOpenClose.text.toString() == "关闭电源") "是否关闭电源？" else "是否开启电源？"
                AnyLayer.with(getCurrentAppAty())
                    .contentView(R.layout.alert_dialog_new)
                    .bindData { anyLayer ->
                        anyLayer.contentView.findViewById<TextView>(R.id.tvTitle).setText(title)
                        anyLayer.contentView.findViewById<TextView>(R.id.tvConfirm)
                            .setOnClickListener {
                                changeBatteryStatus(IS_OPEN)
                                anyLayer.dismiss()
                            }
                        anyLayer.contentView.findViewById<ImageView>(R.id.ivClose)
                            .setOnClickListener {
                                anyLayer.dismiss()
                            }
                    }.backgroundColorInt(Color.parseColor("#85000000"))
                    .backgroundBlurRadius(10f)
                    .backgroundBlurScale(10f)
                    .show()
            }

        }
        btn_continue_rant.setOnClickListener {
            if (checkStatus()){
                doContinueRant()
            }
        }



    }


    private fun changeRemark() {

        val dialog = QMUIDialog.EditTextDialogBuilder(activity)
            .setPlaceholder("请输入电池备注信息")
            .addAction("取消") { dialog, index -> dialog.dismiss() }
        dialog.addAction("确定") { dlg, index ->
            val remark = dialog.editText.text.toString()


            if (remark.isBlank()) {
                EasyToast.DEFAULT.show("请输入电池备注信息")
                return@addAction
            }

            http {
                url = Path.SET_DEVICE_REMARK
                params["device_id"] = CURRENT_DEVICEID
                params["remark"] = remark

                onSuccessWithMsg { s, msg ->
                    tv_remark_num.text = "${remark}"
                    EasyToast.DEFAULT.show(msg)
                }
            }



            dlg.dismiss()

        }
        dialog.show()
    }

    /**
     * 续租
     */
    private fun doContinueRant() {
        FgtMain.instance?.start(FgtPayRentMoney.newInstance(CURRENT_DEVICEID,FgtPayRentMoney.PAGE_TYPE_UPDATE))
//        rentStep1(CURRENT_DEVICEID,FgtPayRentMoney.PAGE_TYPE_UPDATE)
    }

    class RefreshMyDeviceList

    @Subscribe
    fun refreshBattery(event: RefreshMyDeviceList) {
        srl_home?.autoRefresh()
    }

    @Subscribe
    fun openOrCloseBatter(event: BatteryOpenEvent) {
        IS_OPEN = event.isOpen
        if (IS_OPEN) {
            tvOpenClose.text = "关闭电源"
            tvOpenClose.textColor = Color.WHITE
            tvOpenClose.setCompoundDrawablesWithIntrinsicBounds(activity?.getDrawable(R.mipmap.ic_switch_battery),null,null,null)
            pvBattery.colors = intArrayOf(Color.parseColor("#1CFFE6"),Color.parseColor("#2FE19C"),Color.parseColor("#1CFFE6"))
            tvProgress.setTextColor(Color.parseColor("#29EBB6"))
        } else {
            tvOpenClose.text = "开启电源"
            tvOpenClose.textColor = Color.parseColor("#29EBB6")
            tvOpenClose.setCompoundDrawablesWithIntrinsicBounds(activity?.getDrawable(R.mipmap.ic_switch_battery_close),null,null,null)
            pvBattery.colors = intArrayOf(Color.parseColor("#DEF0E9"),Color.parseColor("#DEF0E9"),Color.parseColor("#DEF0E9"))
            tvProgress.setTextColor(Color.parseColor("#DEF0E9"))
        }
        pvBattery.refreshView()

    }


    private fun changeBatteryStatus(isOpen: Boolean) {
        showProgressDialog("操作电池中")
        http {
            url = Path.OPT_DEVICE
            params["device_id"] = CURRENT_DEVICEID
            params["device_status"] = if (!isOpen) "1" else "2"

            onSuccess {
                //                device_status int  1当前为开 2当前为关
                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val device_status = data.optInt("device_status")
                ToastHelper.shortToast(context,"操作成功")
                srl_home.postDelayed(Runnable {
                    getBatteryDetailInfo(if (CURRENT_DEVICEID.isBlank()) "0" else CURRENT_DEVICEID)
                    IS_OPEN = !IS_OPEN
                },6000)

//                if (device_status == 1) {
//                    openOrCloseBatter(BatteryOpenEvent(true))
//                } else {
//                    openOrCloseBatter(BatteryOpenEvent(false))
//                }
            }
            onFinish {
                dissmissProgressDialog()
            }
        }

    }


    @Subscribe
    fun fillHasItemData(event: BatteryInfoChangeEvent) {
        getBatteryDetailInfo(event.device_id)
    }

    private var signDialog: CustomDialog? = null
    private fun signDialog(context: Context, contractId: String) {
        signDialog = CustomDialog(context, R.layout.dialog_sign)
        signDialog!!.gravity = Gravity.CENTER
        signDialog!!.setCancelable(false)
        signDialog!!.show()

        signDialog!!.setOnItemClickListener(R.id.confirmBtn) {
            signDialog!!.dismiss()
            startFgt(
                FgtContractSignStep1.newInstance(
                    contractId,
                    "",
                    0
                )
            )
        }

    }

    private var deviceDetailBean: DeviceDetailBean.Data? = null
    private var paymentDetailBean :PaymentDetailBean.Data ?= null
    private var paymentCode = 200
    private var deviceCode = 200

    @SuppressLint("SetTextI18n")
    private fun getBatteryDetailInfo(deviceId: String = "0") {
        CURRENT_DEVICEID = deviceId

        http {
            url = "/apiv4/getonedevice"
            params["device_id"] = deviceId
            IS_SHOW_MSG = false

            onSuccess { res ->
                deviceDetailBean = res.toPOJO<DeviceDetailBean>().data
                deviceCode = 200
                rent_day = deviceDetailBean!!.device_contract.rent_day
                rent_time = deviceDetailBean!!.device_contract.rent_time
                CURRENT_DEVICEID = "${deviceDetailBean!!.device_id}"
                getPaymentInfo()
            }
            onFail { i, s ->
                Config.getDefault().spUtils.put(KEY_LAST_DEVICE_ID, "")
//                CURRENT_DEVICEID = ""
                deviceCode = i
                getPaymentInfo()
            }
            onFinish {

            }
        }

    }
    var activeStatus = "1" //1-生效中，2-已过期，3-已冻结
    private fun getPaymentInfo(){
        http {
            url = "/apiv6/payment/getuserpaymentinfo"
            params["user_id"] = userId
            params["device_id"] = CURRENT_DEVICEID
            IS_SHOW_MSG = false
            onSuccess {res->
                paymentCode = 200
                paymentDetailBean = res.toPOJO<PaymentDetailBean>().data
                NO_PAY_DEVICEID = paymentDetailBean!!.device_id
                modelName = paymentDetailBean!!.battery.model_name
                totalvoltage = paymentDetailBean!!.battery.totalvoltage
                rsoc = paymentDetailBean!!.battery.rsoc
                deposit = paymentDetailBean!!.deposit
                payType = paymentDetailBean!!.pay_type
                contractId = paymentDetailBean!!.contract_id
                activeStatus = paymentDetailBean!!.active_status
                if (deviceCode == 201){
                    http {
                        url = "/apiv4/getonedevice"
                        params["device_id"] = NO_PAY_DEVICEID
                        IS_SHOW_MSG = false
                        onSuccess { res ->
                            deviceDetailBean = res.toPOJO<DeviceDetailBean>().data
                            deviceCode = 200
                            rent_day = deviceDetailBean!!.device_contract.rent_day
                            rent_time = deviceDetailBean!!.device_contract.rent_time
                            CURRENT_DEVICEID = "${deviceDetailBean!!.device_id}"
                            showHomePageInfo()
                        }
                        onFail { i, s ->
                            Config.getDefault().spUtils.put(KEY_LAST_DEVICE_ID, "")
                            deviceCode = i
                            showHomePageInfo()
                        }
                    }
                }else{
                    showHomePageInfo()
                }


            }
            onFail { i, s ->
                paymentCode = i
                showHomePageInfo()
            }
            onFinish {
                srl_home?.finishRefresh()
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogHelper.i("data===", "===resultCode===${resultCode}")
        if (resultCode == AppCompatActivity.RESULT_OK) {
            LogHelper.i("data===", "===requestCode===${requestCode}")
            if (requestCode == 1) {
                if (null != data) {
                    val bundle = data.extras ?: return
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        val result = bundle.getString(CodeUtils.RESULT_STRING)
                        val getType = bundle.getString("type")
                        val getContractId = bundle.getString("contract_id")
                        LogHelper.i("data===", "===result===${result}")
                        LogHelper.i("data===", "===getType===${getType}")
                        LogHelper.i("data===", "===getContractId===${getContractId}")
                        if (!TextUtils.isEmpty(result)) {
                            if ("换电开门" == getType) {
                                //扫码开门
//                                requestCgOpenDoor(result!!)
                                startFgt(FgtScanOpen.newInstance(CURRENT_DEVICEID,result!!))
                            } else if ("换电" == getType) {
                                //手动换电
                                val intent = Intent(activity, ChangeElectricOpenDoorActivity::class.java)
                                intent.putExtra("type", "换电")
                                intent.putExtra("contract_id", getContractId)
                                intent.putExtra("new_deviceid", result)
                                intent.putExtra("name", if (paymentDetailBean != null && paymentDetailBean!!.paymentInfo != null) paymentDetailBean!!.paymentInfo.pname else "")
                                startActivity(intent)
                            }else if ("租电" == getType){
                                if (result != null) {
                                    scanBox(result)
                                }
                            }else if ("退还" == getType){
                                if (result != null) {
                                    NormalDialog(activity)
                                        .apply {
                                            style(NormalDialog.STYLE_TWO)
                                            btnNum(2)
                                            title("提示")
                                            content("请将电池放入电柜，退还结束后，剩余套餐将清零，请确认操作！")
                                            btnText("确认", "取消")
                                            setOnBtnClickL(OnBtnClickL {
                                                dismiss()
                                                returnBattery(result)
                                            }, OnBtnClickL {
                                                dismiss()
                                            })

                                        }.show()

                                }
                            }else if ("冻结" == getType){
                                if (result != null) {
                                    NormalDialog(activity)
                                        .apply {
                                            style(NormalDialog.STYLE_TWO)
                                            btnNum(2)
                                            title("冻结套餐需要将电池归还至电柜。冻结成功后套餐将暂停计费，如需恢复套餐，需要您进行解冻操作")
                                            content("请确认是否立即开始冻结？")
                                            btnText("确认", "取消")
                                            setOnBtnClickL(OnBtnClickL {
                                                dismiss()
                                               frozen(result)
                                            }, OnBtnClickL {
                                                dismiss()
                                            })

                                        }.show()

                                }
                            }
                        }
                    } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                        ToastHelper.shortToast(activity, "解析二维码失败")
                    }
                }
            }
        }
    }

    /**
     * 柜内租用电池
     */
    private fun scanBox(code: String) {
        http {
            url = "apiv6/cabinet/rentBattery"
            params["user_id"] = userId
            params["code"] = code
            onSuccessWithMsg { res, msg ->
                ToastHelper.shortToast(activity, msg)
                srl_home?.autoRefresh()
            }

            onFail { i, msg ->
                ToastHelper.shortToast(activity, msg)
            }
        }
    }
    private fun autoRefresh(){
        srl_home.postDelayed( { srl_home.autoRefresh() },2000)
    }
    /**
     * 柜内租用电池
     */
    private fun returnBattery(code: String) {
        http {
            url = "apiv6/cabinet/retrunBattery"
            params["user_id"] = userId
            params["code"] = code
            params["device_id"] = CURRENT_DEVICEID
            onSuccessWithMsg { res, msg ->
                ToastHelper.shortToast(activity, "请将电池放入电柜，后台自动审核")

                autoRefresh()
            }

            onFail { i, msg ->
                ToastHelper.shortToast(activity, msg)
            }
        }
    }

    private fun requestCgOpenDoor(code: String) {
        http {
            url = "apiv4/cgopendoor"
            params["code"] = code

            onSuccessWithMsg { res, msg ->
                ToastHelper.shortToast(activity, msg)
            }

            onFail { i, msg ->
                ToastHelper.shortToast(activity, msg)
            }
        }
    }

    var virtaul = false
    private fun showNewDeviceData(item:DeviceDetailBean.Data){
        CURRENT_DEVICEID = item.device_id.toString()
        CURRENT_CONTRACT_ID = item.device_contract.contract_id
        Config.getDefault().spUtils.put(KEY_LAST_DEVICE_ID, CURRENT_DEVICEID)
        showPopMsg(item.popmsg)
        showDeviceInfo(item.device_base)
        showBatteryInfo(item.device_base)
        if (!virtaul){
            tv_remark_num.text = item.device_contract.remark
        }
//        if ("0" == item.device_contract.is_sign) {
//            signDialog(activity!!, item.device_contract.contract_id)
//        }
        initInfoEvent(item)
        tabBattery.selectTab(0)
        layoutPackage.visibility =View.VISIBLE
        layoutBattery.visibility =View.GONE
    }

    private fun showDeviceInfo(info:DeviceDetailBean.Data.DeviceBase){
        if (info.protect != "0" && info.protect != "4096") {
            //故障状态
            ivWrongBt.visibility  = View.VISIBLE
            tvProgress.setTextColor(Color.parseColor("#FFE177"))
            pvBattery.colors = intArrayOf(Color.parseColor("#FFE177"),Color.parseColor("#FF7A5A"),Color.parseColor("#FFE177"))
            tv_error_title.visibility = View.VISIBLE
            tv_error_info.visibility = View.VISIBLE
            tv_error_info.text = info.protect_desc
        }else{
            // 通电或者关电状态
            ivWrongBt.visibility  = View.GONE
            openOrCloseBatter(BatteryOpenEvent(info.device_status == "1"))
            tv_error_title.visibility = View.GONE
            tv_error_info.visibility = View.GONE
        }
        tv_ice.text = "冻结"
        tv_ice.setCompoundDrawablesWithIntrinsicBounds(null,
            context?.getDrawable(R.mipmap.ic_stop_contract),null,null)
        tvBatteryName.text = info.device_id
        if (info.device_id.startsWith("8") && info.device_id.length == 8 ){
            tv_remark_num.text = "虚拟编号"
            tvProgress.textColor = Color.parseColor("#29EBB6")
            tv_please_change.visibility = View.VISIBLE
            tv_left_battery.visibility = View.GONE
            tv_voltage.visibility = View.GONE
            pvBattery.visibility = View.GONE
            virtaul = true
           if (paymentDetailBean?.active_status  == "1"){
               tvProgress.text = "待取电"
               tv_please_change.text = "(请进行\"扫码换电\")"
               tv_package_status.text = "生效中"
               tv_package_status.background = context?.getDrawable( R.drawable.shape_green)
           }else if (paymentDetailBean?.active_status  == "3"){
               tvProgress.text = "已冻结"
               tv_please_change.text = "(请进行\"解冻\"操作)"
               tv_ice.text = "解冻"
               tv_ice.setCompoundDrawablesWithIntrinsicBounds(null,
                   context?.getDrawable(R.mipmap.ic_scan_box),null,null)
               tv_package_status.text = "已冻结"
               tv_package_status.background = context?.getDrawable( R.drawable.shape_yello)
           }

        }else{
            virtaul = false
            pvBattery.maxCount = 100f
            pvBattery.setCurrentCount(info.rsoc.toFloat())
            tvProgress.text = "${info.rsoc}%"
            tv_voltage.text = "电压 ${info.totalvoltage}V"
            tv_please_change.visibility = View.GONE
            tv_left_battery.visibility = View.VISIBLE
            tv_voltage.visibility = View.VISIBLE
            pvBattery.visibility = View.VISIBLE

        }
    }

    private fun showBatteryInfo(info:DeviceDetailBean.Data.DeviceBase){
        //详细信息
        tv_detail_standard_ah.text = info.standardcapacity + "A"
        tv_detail_total_u.text = info.totalvoltage + "V"
        tv_detail_total_i.text = info.electric + "A"
        tv_detail_temperature.text = info.temperature + "°C"
        tv_detail_protect_status.text = info.protect
        tv_detail_fet.text = info.fet
        tv_detail_software_ver.text = info.softversion
    }
    private fun showPackageInfo(){
        tv_ice.text = if (activeStatus == "3") "解冻" else "冻结"

        hasChangePackege = false
        if (paymentDetailBean != null){


            tv_ya_monety.text =  "${paymentDetailBean!!.deposit}元"
            tv_rent_money.text = "${paymentDetailBean!!.rent_money}元"
            tv_ya_monety.setOnClickListener {
                if ( tv_ya_monety.text != "0.00"){
                    startFgt(FgtMeDeposit())
                }
            }
            if (paymentDetailBean!!.paymentInfo != null){
                modelName = paymentDetailBean!!.paymentInfo.modelName
                tv_package_name.text = paymentDetailBean!!.paymentInfo.pname
                tv_package_time.text = TextUtil.formatTime(paymentDetailBean!!.begin_time,paymentDetailBean!!.exp_time)
                if (paymentDetailBean!!.exp_remind == 1){
                    tv_exp_remind.visibility =  View.VISIBLE
                    tv_exp_remind.text = paymentDetailBean!!.exp_remind_msg
                }else{
                    tv_exp_remind.visibility =  View.GONE
                }

                tv_change_package_type.text = "次数无限制"
                tv_change_package_left_times.visibility = GONE
                tv_change_package_time.text =  tv_package_time.text
                tv_no_package.visibility = View.GONE
                tv_btn_change_package.visibility = View.GONE
                tv_more.visibility = View.GONE
                tv_btn_change_package_update.visibility = GONE

//                val options = ArrayList<PaymentOption>()
//                options.addAll(paymentDetailBean!!.paymentInfo.userOptions.filter { it.option_type == "2" })
//                //看是否有单次换电
//                if ( paymentDetailBean?.singleChangeInfo != null){
//                    val singleOption = paymentDetailBean!!.singleChangeInfo
//                    singleOption.change_times = "1"
//                    singleOption.show_start_time = singleOption.start_time
//                    singleOption.show_end_time = singleOption.end_time
//                    singleOption.name = "单次换电"
//                    singleOption.active_status = "1"
//                    singleOption.single_option = true
//                   options.add(singleOption)
//                }
//                //是否有换电套餐
//                if (!options.isEmpty()){
//                    // 是否有生效套餐
//                    if (options.count { it.active_status == "1" } > 0){
//                        cl_change_package.visibility = View.VISIBLE
//                        tv_no_package.visibility = View.GONE
//                        var option  = options[0]
//                        if (option != null){
//                            tv_change_package_type.text = "次数无限制"
////                            tv_change_package_left_times.text = "剩余${option.change_times}次"
//                            tv_change_package_time.text = tv_package_time.text
////                            val statusRes = if(option.active_status == "1")  R.mipmap.ic_pakage_status01 else R.mipmap.ic_pakage_status02
////                            tv_change_package_title.setCompoundDrawablesWithIntrinsicBounds(null,null,
////                                context?.let { ContextCompat.getDrawable(it,statusRes) },null)
//                        }
//
//                        hasChangePackege = true
//                        tv_btn_change_package.visibility = View.GONE
//                        tv_more.visibility = if(options.size > 1) View.VISIBLE else View.GONE
////                        tv_btn_change_package_update.visibility = if(options.size <= 1) View.VISIBLE else View.GONE
//                        tv_btn_change_package_update.visibility = View.GONE
//                        tv_btn_change_package_update.setOnClickListener {
//                            doContinueRant()
//                        }
//                        tv_more.setOnClickListener {
//                            activity?.let { it1 -> ChangePackageListPopup(it1, options) }
//                        }
//                    }else{
//                        cl_change_package.visibility = View.GONE
//                        tv_btn_change_package.text = "立即启用"
//                        tv_no_package.text = "您有待生效套餐未启用"
//                        tv_no_package.visibility = View.VISIBLE
//                        tv_btn_change_package.visibility = View.VISIBLE
//                        tv_btn_change_package.setOnClickListener {
//                            activeOption(options.filter { it.active_status =="2" }.first().id)
//                        }
//                    }
//                }else{
//                    cl_change_package.visibility = View.GONE
//                    tv_btn_change_package.text = "购买换电套餐"
//                    tv_no_package.visibility = View.VISIBLE
//                    tv_btn_change_package_update.visibility = View.GONE
//                    tv_btn_change_package.visibility = View.VISIBLE
//                    tv_btn_change_package.setOnClickListener {
//                        buyChangePackage()
//                    }
//                }
            }

        }
    }

    private fun activeOption(id:String){
        http {
            url = "/apiv6/payment/activeoption"
            params["user_option_id"] = "${id}"
            onSuccess {
                ToastHelper.shortToast(context,"启用成功")
                getBatteryDetailInfo(if (CURRENT_DEVICEID.isBlank()) "0" else CURRENT_DEVICEID)
            }
            onFail { i, s ->
                ToastHelper.shortToast(context,s)
            }
        }
    }

    private  fun   checkStatus(): Boolean {
        if (activeStatus == "3"){
            ToastHelper.shortToast(context,"请先完成解冻操作")
            return false
        }else if (virtaul){
            ToastHelper.shortToast(context,"请先完成取电操作")
            return false
        }
        return true
    }
    private fun initInfoEvent(item:DeviceDetailBean.Data){
        contractId = item.device_contract.contract_id
        tvFindLocation.setOnClickListener {
            if (checkStatus()){
                 getPermissions(activity,
                PermissionType.COARSE_LOCATION,
                PermissionType.FINE_LOCATION,
                PermissionType.ACCESS_WIFI_STATE,
                PermissionType.ACCESS_NETWORK_STATE,
                allGranted = {
                    startFgt(FgtFindBattery())
                })
            }

        }
        tvWay.setOnClickListener {
            if (checkStatus()){startFgt(FgtTrajectory())} }
        tvSwitch.setOnClickListener {
            getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                val intent = Intent(activity, ScanQrCodeActivity::class.java)
                intent.putExtra("type", "换电")
                intent.putExtra("contract_id", item.device_contract.contract_id)
                startActivityForResult(intent, 1)
            })

        }
        tv_scan_box.setOnClickListener {
            http {
                url = Path.GET_MY_DEVICE
                onSuccess {
                    val result = it.toPOJO<MyDevicesBean>().data
                    if (result.size >=2 ){
                        ToastHelper.shortToast(context,"可租用电池数已达上限(2)")
                    }else{
                        ToastHelper.shortToast(context,"请扫描电柜二维码")
                        getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
//                            val intent = Intent(activity, ScanQrCodeActivity::class.java)
//                            intent.putExtra("type", "租电")
//                            startActivityForResult(intent, 1)
                            tryToScan()
                        })
                    }
                }
            }

        }
        tvReback.setOnClickListener {
            //3.【退还】按钮点击后，如果判断是虚拟电池：“8开头 + 8位”，就走老流程，跳转到“退还”页面（需要上传图片、填写原因那个）
            //否则，进入扫码页，获取到电柜码，调用新接口returnBattery】
            if (activeStatus == "3"){
                ToastHelper.shortToast(context,"没有需要退还的电池")
            }else{
                if (CURRENT_DEVICEID.startsWith("8") && CURRENT_DEVICEID.length == 8){
//                    startFgt(FgtReturn.newInstance(CURRENT_DEVICEID))
                    ToastHelper.shortToast(context,"没有需要退还的电池")
                }else{
                    ToastHelper.shortToast(context,"请扫描电柜二维码")
                    getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                        val intent = Intent(activity, ScanQrCodeActivity::class.java)
                        intent.putExtra("type", "退还")
                        startActivityForResult(intent, 1)
                    })
                }
            }


        }
        changeOpenDoor?.setOnClickListener {
            if (activeStatus == "3"){
                ToastHelper.shortToast(context,"请先完成解冻操作")
            }else{
                ToastHelper.shortToast(context,"请扫描电柜二维码")
//                if (hasChangePackege){
                    getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                        val intent = Intent(activity, ScanQrCodeActivity::class.java)
                        intent.putExtra("type", if(virtaul) "租电"  else "换电开门")
                        startActivityForResult(intent, 1)
                    })
//                }else{
//                    buyChangePackage()
//                }
            }

        }

        tvUnbind.setOnClickListener {
            http {
                url = Path.UNBIND_BATTERY
                params["contract_id"] = item.device_contract.contract_id
                params["device_id"] = item.device_id.toString()
                onSuccessWithMsg { res, msg ->
                    EasyToast.DEFAULT.show(msg)
                    getBatteryDetailInfo()
                }
            }
        }
        tv_ice.setOnClickListener {
            if (tv_ice.text.equals("冻结")){
                if (virtaul){
                    NormalDialog(activity)
                        .apply {
                            style(NormalDialog.STYLE_TWO)
                            btnNum(2)
                            title("冻结套餐成功后套餐将暂停计费，如需恢复套餐，需要您进行解冻操作")
                            content("请确认是否立即开始冻结？")
                            btnText("确认", "取消")
                            setOnBtnClickL(OnBtnClickL {
                                dismiss()
                                frozen("")
                            }, OnBtnClickL {
                                dismiss()
                            })

                        }.show()
                }else{
                    ToastHelper.shortToast(context,"请扫描电柜二维码")
                    getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                        val intent = Intent(activity, ScanQrCodeActivity::class.java)
                        intent.putExtra("type", "冻结")
                        startActivityForResult(intent, 1)
                    })
                }
            }else{
                NormalDialog(activity)
                    .apply {
                        style(NormalDialog.STYLE_TWO)
                        btnNum(2)
                        title("解冻成功后套餐将立即恢复计费，之后您可扫码取电继续使用")
                        content("请确认是否立即开始解冻？")
                        btnText("确认", "取消")
                        setOnBtnClickL(OnBtnClickL {
                            dismiss()
                            resumeBattery()
                        }, OnBtnClickL {
                            dismiss()
                        })

                    }.show()
            }

        }
    }
    private fun frozen( code:String){
        http {
            url = "/apiv6/cabinet/frozen"
            params["device_id"] = CURRENT_DEVICEID
            params["user_id"] = userId
            if (code != ""){
                params["code"] = code
            }
            onSuccess {
                if (code != ""){
                    ToastHelper.shortToast(context,"请将电池放入电柜，然后刷新页面")
                }else{
                    ToastHelper.shortToast(context,"操作成功")
                }
                autoRefresh()
            }
            onFail { i, s ->  }
        }
    }
    private fun resumeBattery( ){
        http {
            url = "/apiv6/cabinet/resume"
            params["device_id"] = CURRENT_DEVICEID
            params["user_id"] = userId

            onSuccess {
                ToastHelper.shortToast(context,"操作成功")

                autoRefresh()
            }
            onFail { i, s ->  }
        }
    }
    /**
     * 购买换电套餐
     */
    private fun buyChangePackage() {
        ToastHelper.shortToast(context,"当前没有换电套餐，请先购买换电套餐")
        activity.let {
            if (it != null) {
                var listener = View.OnClickListener {
                    when (it.id) {
                        R.id.option1 -> FgtMain.instance?.start(FgtSinglePay.newInstance(CURRENT_DEVICEID))
                        R.id.option2 -> FgtMain.instance?.start(FgtPayRentMoney.newInstance(CURRENT_DEVICEID,FgtPayRentMoney.PAGE_TYPE_UPDATE))
                    }
                }
                BuyChangePackagePopup(it, listener)
            }

        }
    }



    private fun initTabLayout() {
        tabBattery.apply {
            reset()
            addTab(QMUITabSegment.Tab("电池套餐信息"))
            addTab(QMUITabSegment.Tab("电池详细信息"))
            setTabTextSize(sp(14))
            setHasIndicator(true)
            setIndicatorWidthAdjustContent(true)
            setIndicatorDrawable(resources.getDrawable(R.drawable.line_green))
            setDefaultNormalColor(Color.parseColor("#8694A0"))
            setDefaultSelectedColor(resources.getColor(R.color.white))
            selectTab(0)
            notifyDataChanged()
            setOnTabClickListener {  index ->
                layoutPackage.visibility = if (index == 0) View.VISIBLE else View.GONE
                layoutBattery.visibility = if (index == 1) View.VISIBLE else View.GONE
            }
        }

    }
    private fun showPopMsg(popmsg:DeviceDetailBean.Data.PopMsgBean){
        if (1 == popmsg.show_msg) {
            CommonPromptDialogHelper.promptCommonDialog(
                activity!!,
                "",
                popmsg.msg,
                "",
                "",
                true,
                false,
                false,
                true,
                object : CommonDialogCallBackHelper {
                    override fun back(viewId: Int, msg: String?) {

                    }
                }
            )
        }
    }


}