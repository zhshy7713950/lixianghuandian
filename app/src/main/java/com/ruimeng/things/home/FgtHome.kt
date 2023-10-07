package com.ruimeng.things.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.ruimeng.things.*
import com.ruimeng.things.home.bean.DeviceDetailBean
import com.ruimeng.things.home.bean.PaymentDetailBean
import com.ruimeng.things.home.bean.PaymentOption
import com.ruimeng.things.home.view.BuyChangePackagePopup
import com.ruimeng.things.home.view.ChangePackageListPopup
import com.ruimeng.things.me.FgtTrueName
import com.ruimeng.things.me.contract.FgtContractSignStep1
import com.utils.*
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.activity_balance_withdrawal.*
import kotlinx.android.synthetic.main.fgt_home.*
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
import kotlin.math.sin


/**
 * Created by wongxd on 2018/11/9.
 */
class FgtHome : MainTabFragment() {

    companion object {
        const val REQUEST_ZXING_CODE = 1025
        const val KEY_LAST_DEVICE_ID = "lastDeviceId"

        var CURRENT_DEVICEID = ""
        var NO_PAY_DEVICEID = ""
        var CURRENT_CONTRACT_ID = ""
        var IS_OPEN = false

        var IsWholeBikeRent = true
        var getIsHost = "1"
        var deviceStatus = 0 //0 有押金 有租金 1，无押金，无租金  2 有押金 无租金
        var userId = ""
        var hasChangePackege = false //是否有换电套餐
        var modelName = ""
        var contractId = ""

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

        /**
         * 处理扫码后的信息
         */
        fun dealScanResult(deviceId: String?,type:Int = FgtPayRentMoney.PAGE_TYPE_CREATE) {

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


        val userInfo = InfoViewModel.getDefault().userInfo.value
        userInfo?.let {
            if (userInfo.mobile.isBlank() && userInfo.mobile_bind != "1") {
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


    private fun initNoItemView() {
        if (deviceCode == 201 && paymentCode == 208){
            deviceStatus = 1
        }else if (deviceCode == 201 && paymentCode == 200){
            deviceStatus = 2
        }else{
            deviceStatus = 0
        }
       if (deviceStatus == 2){
            tv_log_info.text ="您还没有为电池（编号"+ NO_PAY_DEVICEID+"）购买套餐"
            tv_add_device.text  = "点击购买套餐"
            iv_add_device.visibility = View.GONE
        }else{
            tv_add_device.text  = "点击添加"
            iv_add_device.visibility = View.VISIBLE
        }

        val llNoItem = root_no_item
        val addDeviceBtn = llNoItem.findViewById<FrameLayout>(R.id.addDeviceBtn)
        addDeviceBtn.setOnClickListener {
            when (deviceStatus) {
                0-> tryToScan()
                1 -> tryToScan()
                2 -> dealScanResult(NO_PAY_DEVICEID)
            }
        }

    }


    private fun initHasItemView() {

        tv_remark_num.setOnClickListener { changeRemark() }
        fl_change_remark_home.setOnClickListener { changeRemark() }

        tv_switch_battery.setOnClickListener { startFgt(FgtSwitchBattery()) }
        tvOpenClose.setOnClickListener {
            var title = if (tvOpenClose.text.toString() == "关闭电源") "是否关闭电源？" else "是否开启电源？"
            AnyLayer.with(getCurrentAppAty())
                .contentView(R.layout.alert_dialog_new)
                .bindData { anyLayer ->
                    anyLayer.contentView.findViewById<TextView>(R.id.tvTitle).setText(title)
                    anyLayer.contentView.findViewById<TextView>(R.id.tvConfirm).setOnClickListener{
                        changeBatteryStatus(IS_OPEN)
                        anyLayer.dismiss()
                    }
                    anyLayer.contentView.findViewById<ImageView>(R.id.ivClose).setOnClickListener{
                        anyLayer.dismiss()
                    }
                }.backgroundColorInt(Color.parseColor("#85000000"))
                .backgroundBlurRadius(10f)
                .backgroundBlurScale(10f)
                .show()
        }
        btn_continue_rant.setOnClickListener { doContinueRant() }



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
        dealScanResult(CURRENT_DEVICEID,FgtPayRentMoney.PAGE_TYPE_UPDATE)
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
            params["device_status"] = if (isOpen) "1" else "2"

            onSuccess {
                //                device_status int  1当前为开 2当前为关
                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val device_status = data.optInt("device_status")
                getBatteryDetailInfo(if (CURRENT_DEVICEID.isBlank()) "0" else CURRENT_DEVICEID)
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


        http {
            url = "/apiv4/getonedevice"
            params["device_id"] = deviceId
            IS_SHOW_MSG = false

            onSuccess { res ->
                srl_home?.let {
                    dealTwoStatus(true)
                    deviceDetailBean = res.toPOJO<DeviceDetailBean>().data
                    deviceDetailBean?.let { item ->
                        showNewDeviceData(item)
                    }
                }
                deviceCode = 200
                getPaymentInfo()
            }
            onFail { i, s ->
                Config.getDefault().spUtils.put(KEY_LAST_DEVICE_ID, "")
                CURRENT_DEVICEID = ""
                deviceCode = i
                getPaymentInfo()
            }
        }

    }
    private fun getPaymentInfo(){
        http {
            url = "/apiv6/payment/getuserpaymentinfo"
            params["user_id"] = userId
            params["device_id"] = CURRENT_DEVICEID
            IS_SHOW_MSG = false
            onSuccess {res->
                paymentCode = 200
                srl_home?.let {
                    paymentDetailBean = res.toPOJO<PaymentDetailBean>().data
                    NO_PAY_DEVICEID = paymentDetailBean!!.device_id
                    initNoItemView()

                    showPackageInfo()

                }
            }
            onFail { i, s ->
                paymentCode = i
                initNoItemView()
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
                            } else {
                                //手动换电
                                val intent =
                                    Intent(activity, ChangeElectricOpenDoorActivity::class.java)
                                intent.putExtra("type", "换电")
                                intent.putExtra("contract_id", getContractId)
                                intent.putExtra("new_deviceid", result)
                                intent.putExtra("name", if (paymentDetailBean != null && paymentDetailBean!!.paymentInfo != null) paymentDetailBean!!.paymentInfo.pname else "")
                                startActivity(intent)
                            }
                        }
                    } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                        ToastHelper.shortToast(activity, "解析二维码失败")
                    }
                }
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

    private fun showNewDeviceData(item:DeviceDetailBean.Data){
        CURRENT_DEVICEID = item.device_id.toString()
        CURRENT_CONTRACT_ID = item.device_contract.contract_id
        Config.getDefault().spUtils.put(KEY_LAST_DEVICE_ID, CURRENT_DEVICEID)
        showPopMsg(item.popmsg)
        showDeviceInfo(item.device_base)
        showBatteryInfo(item.device_base)
        tv_remark_num.text = item.device_contract.remark
        if ("0" == item.device_contract.is_sign) {
            signDialog(activity!!, item.device_contract.contract_id)
        }
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
        pvBattery.maxCount = 100f
        pvBattery.setCurrentCount(info.rsoc.toFloat())
        tvProgress.text = "${info.rsoc}%"
        tv_voltage.text = "电压 ${info.totalvoltage}V"
        tvBatteryName.text = info.device_id
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
        hasChangePackege = false
        if (paymentDetailBean != null){


            tv_ya_monety.text =  "${paymentDetailBean!!.deposit}元"
            tv_rent_money.text = "${paymentDetailBean!!.rent_money}元"
            if (paymentDetailBean!!.paymentInfo != null){
                modelName = paymentDetailBean!!.paymentInfo.modelName
                tv_package_name.text = paymentDetailBean!!.paymentInfo.pname
                tv_package_time.text = TextUtil.formatTime(paymentDetailBean!!.begin_time,paymentDetailBean!!.exp_time)
                //看是否有单次换电
                val options = ArrayList<PaymentOption>()
                if ( paymentDetailBean?.singleChangeInfo != null){
                    val singleOption = paymentDetailBean!!.singleChangeInfo
                   singleOption.change_times = "1"
                    singleOption.show_start_time = singleOption.start_time
                    singleOption.show_end_time = singleOption.end_time
                    singleOption.name = "单次换电"
                    singleOption.active_status = "1"
                    singleOption.single_option = true
                   options.add(singleOption)
                }
                options.addAll(paymentDetailBean!!.paymentInfo.userOptions.filter { it.option_type == "2" })
                //是否有换电套餐
                if (!options.isEmpty()){
                    // 是否有生效套餐
                    if (options.count { it.active_status == "1" } > 0){
                        cl_change_package.visibility = View.VISIBLE
                        tv_no_package.visibility = View.GONE
                        var option  = options[0]
                        if (option != null){
                            tv_change_package_type.text = if(option.single_option) "单次换电" else "换电${option.total_times}次"
                            tv_change_package_left_times.text = "剩余${option.change_times}次"
                            tv_change_package_time.text = TextUtil.formatTime(option.start_time,option.end_time)
                            val statusRes = if(option.active_status == "1")  R.mipmap.ic_pakage_status01 else R.mipmap.ic_pakage_status02
                            tv_change_package_title.setCompoundDrawablesWithIntrinsicBounds(null,null,
                                context?.let { ContextCompat.getDrawable(it,statusRes) },null)
                        }

                        hasChangePackege = true
                        tv_btn_change_package.visibility = View.GONE
                        tv_more.visibility = if(options.size > 1) View.VISIBLE else View.GONE
                        tv_btn_change_package_update.visibility = if(options.size <= 1) View.VISIBLE else View.GONE
                        tv_btn_change_package_update.setOnClickListener {
                            doContinueRant()
                        }
                        tv_more.setOnClickListener {
                            activity?.let { it1 -> ChangePackageListPopup(it1, options) }
                        }
                    }else{
                        cl_change_package.visibility = View.GONE
                        tv_btn_change_package.text = "立即启用"
                        tv_no_package.text = "您有待生效套餐未启用"
                        tv_no_package.visibility = View.VISIBLE
                        tv_btn_change_package.visibility = View.VISIBLE
                        tv_btn_change_package.setOnClickListener {
                            activeOption(options.filter { it.active_status =="2" }.first().option_id)
                        }
                    }
                }else{
                    cl_change_package.visibility = View.GONE
                    tv_btn_change_package.text = "购买换电套餐"
                    tv_no_package.visibility = View.VISIBLE
                    tv_btn_change_package_update.visibility = View.GONE
                    tv_btn_change_package.visibility = View.VISIBLE
                    tv_btn_change_package.setOnClickListener {
                        buyChangePackage()
                    }
                }
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

    private fun initInfoEvent(item:DeviceDetailBean.Data){
        contractId = item.device_contract.contract_id
        tvFindLocation.setOnClickListener {
            getPermissions(activity,
                PermissionType.COARSE_LOCATION,
                PermissionType.FINE_LOCATION,
                PermissionType.ACCESS_WIFI_STATE,
                PermissionType.ACCESS_NETWORK_STATE,
                allGranted = {
                    startFgt(FgtFindBattery())
                })
        }
        tvWay.setOnClickListener { startFgt(FgtTrajectory()) }
        tvSwitch.setOnClickListener {
            getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                val intent = Intent(activity, ScanQrCodeActivity::class.java)
                intent.putExtra("type", "换电")
                intent.putExtra("contract_id", item.device_contract.contract_id)
                startActivityForResult(intent, 1)
            })

        }
        tvReback.setOnClickListener {startFgt(FgtReturn())  }
        changeOpenDoor?.setOnClickListener {
            if (hasChangePackege){
                getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                    val intent = Intent(activity, ScanQrCodeActivity::class.java)
                    intent.putExtra("type", "换电开门")
                    startActivityForResult(intent, 1)
                })
            }else{
                buyChangePackage()
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