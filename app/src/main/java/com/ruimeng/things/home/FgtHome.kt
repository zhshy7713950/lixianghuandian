package com.ruimeng.things.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundFrameLayout
import com.ruimeng.things.*
import com.ruimeng.things.home.bean.DeviceDetailBean
import com.ruimeng.things.me.FgtTrueName
import com.ruimeng.things.me.contract.FgtContractSignStep1
import com.ruimeng.things.me.credit.FgtCreditReckoning
import com.utils.*
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.fgt_home.*
import kotlinx.android.synthetic.main.home_status_item.*
import kotlinx.android.synthetic.main.home_status_no_item.*
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import wongxd.Config
import wongxd.base.MainTabFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.*
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.http
import wongxd.utils.utilcode.util.ScreenUtils


/**
 * Created by wongxd on 2018/11/9.
 */
class FgtHome : MainTabFragment() {

    companion object {
        const val REQUEST_ZXING_CODE = 1025
        const val KEY_LAST_DEVICE_ID = "lastDeviceId"

        var CURRENT_DEVICEID = ""
        var CURRENT_CONTRACT_ID = ""
        var IS_OPEN = false

        var IsWholeBikeRent = true
        var getIsHost = "1"


        /**
         * 选择是 租赁整车 还是 租赁电池
         */
        fun selectDeviceType() {

            fun doTryScan(anyLayer: AnyLayer) {
                anyLayer.dismiss()
                tryToScan()
            }

            AnyLayer.with(getCurrentAppAty())
                .contentView(R.layout.layout_check_add_device_type)
                .bindData { anyLayer ->
                    val flWholeBike =
                        anyLayer.contentView.findViewById<FrameLayout>(R.id.fl_whole_bike)
                    val flBattery = anyLayer.contentView.findViewById<FrameLayout>(R.id.fl_battery)
                    val ivWholeBike =
                        anyLayer.contentView.findViewById<ImageView>(R.id.iv_check_whole_bike)
                    val ivBattery =
                        anyLayer.contentView.findViewById<ImageView>(R.id.iv_check_battery)


                    val btnSubmit =
                        anyLayer.contentView.findViewById<QMUIRoundButton>(R.id.btn_submit)


                    fun resetCheckState() {
                        ivWholeBike.setImageResource(if (IsWholeBikeRent) R.drawable.icon_rent_type_checked else R.drawable.icon_rent_type_uncheck)
                        ivBattery.setImageResource(if (!IsWholeBikeRent) R.drawable.icon_rent_type_checked else R.drawable.icon_rent_type_uncheck)
                    }

                    flWholeBike.setOnClickListener {
                        IsWholeBikeRent = true
                        getIsHost = "1"
                        resetCheckState()
                    }

                    flBattery.setOnClickListener {
                        IsWholeBikeRent = false
                        getIsHost = "2"
                        resetCheckState()
                    }

                    btnSubmit.setOnClickListener {
                        doTryScan(anyLayer)
                    }
                    resetCheckState()
                }
                .backgroundColorInt(Color.parseColor("#85000000"))
                .backgroundBlurRadius(10f)
                .backgroundBlurScale(10f)
                .show()
        }


        /**
         * 尝试扫描二维码
         */
        fun tryToScan(prefix: String = "", oldContractId: String = "", isHost: String = "") {
            getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                Log.i("data===", "===contract_id2===${oldContractId}")
                AtyScanQrcode.start(getCurrentAty(), prefix, oldContractId, isHost)
            })

        }

        /**
         * 处理扫码后的信息
         */
        fun dealScanResult(deviceId: String?) {

            deviceId ?: return

            http {
                url = "apiv4/rentstep1"
                params["device_id"] = deviceId
                params["cg_mode"] = Config.getDefault().spUtils.getString("cg_mode", "0")
                onSuccess {
                    //{"errcode":200,"errmsg":"\u64cd\u4f5c\u6210\u529f","data":{"status":0}}
                    //status int 状态， 0 等待支付押金 -》进入押金支付界面，1 押金已支付 进入租金支付界面
                    val json = JSONObject(it)
                    val data = json.optJSONObject("data")
                    val status = data.optInt("status")

                    when (status) {
                        0 -> FgtMain.instance?.start(FgtDeposit.newInstance(deviceId, getIsHost))
                        1 -> FgtMain.instance?.start(FgtPayRentMoney.newInstance(deviceId))
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

        topbar = rootView.findViewById(R.id.topbar)
        initTopbar(topbar, "设备状态", false)


        if (!TextUtils.isEmpty(Config.getDefault().spUtils.getString("ledString", ""))) {
            ledTextView?.text =  Config.getDefault().spUtils.getString("ledString", "")
            ledTextView?.init(activity?.windowManager)
            ledTextView?.startScroll()
        }


        right?.setOnClickListener { _ -> selectDeviceType() }

        tv_follow_wechat.setOnClickListener { start(FgtFollowWechatAccount()) }


        val userInfo = InfoViewModel.getDefault().userInfo.value
        userInfo?.let {
            if (userInfo.mobile.isBlank() && userInfo.mobile_bind != "1") {
                startFgt(FgtBindPhone())
            }


            tv_follow_wechat.visibility = if (userInfo.mp_follow == 0) View.VISIBLE else View.GONE

            tv_unbind_battery_home.visibility =
                if (userInfo.is_debug == 1) View.VISIBLE else View.GONE
        }

        CURRENT_DEVICEID = Config.getDefault().spUtils.getString(KEY_LAST_DEVICE_ID)

        dealTwoStatus(false)

        WaitViewController.from(root_has_item) { renderChilds() }

        srl_home?.setEnableLoadMore(false)
        srl_home.setOnRefreshListener { getBatteryDetailInfo(if (CURRENT_DEVICEID.isBlank()) "0" else CURRENT_DEVICEID) }

        srl_home.autoRefresh()


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
//        topbar.removeAllRightViews()
        val llNoItem = root_no_item
        val addDeviceBtn = llNoItem.findViewById<QMUIRoundFrameLayout>(R.id.addDeviceBtn)
        addDeviceBtn.setOnClickListener {
            //点击添加
            choiceUseModelDialog(activity!!)
        }

    }

    val right by lazy { topbar?.addRightImageButton(R.drawable.scan, R.id.right) }

    private fun initHasItemView() {

        right?.setOnClickListener { _ -> choiceUseModelDialog(activity!!) }

        iv_change_reamrk_home.setOnClickListener { changeRemark() }
        tv_remark_num.setOnClickListener { changeRemark() }
        fl_change_remark_home.setOnClickListener { changeRemark() }

        tv_switch_battery.setOnClickListener { startFgt(FgtSwitchBattery()) }

        fl_switch.setOnClickListener {
            if (tv_switch_des.text.toString().contains("开启")) {
                changeBatteryStatus(true)
            } else {
                changeBatteryStatus(false)
            }
        }



        btn_continue_rant.setOnClickListener { doContinueRant() }

        ll_enter_device_info.setOnClickListener {
            if (ll_detail_device_info.visibility == View.GONE) {
                ll_detail_device_info.visibility = View.VISIBLE
                iv_enter_device_info.rotation = 90f
            } else {
                ll_detail_device_info.visibility = View.GONE
                iv_enter_device_info.rotation = 0f
            }
        }


        ll_detail_device_info.visibility = View.GONE

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
                    tv_remark_num.text = "备注：${remark}"
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
        dealScanResult(CURRENT_DEVICEID)
    }

    class RefreshMyDeviceList

    @Subscribe
    fun refreshBattery(event: RefreshMyDeviceList) {
        srl_home?.autoRefresh()
    }

    @Subscribe
    fun openOrCloseBatter(event: BatteryOpenEvent) {
        fun changeRoundFramLayoutBg(rfl: QMUIRoundFrameLayout, isOpen: Boolean) {
            val dra = rfl.background as QMUIRoundButtonDrawable
            dra.setBgData(ColorStateList.valueOf(if (isOpen) Color.RED else Color.GREEN))
        }

        IS_OPEN = event.isOpen

        changeRoundFramLayoutBg(fl_switch, IS_OPEN)

        iv_switch.setColorFilter(Color.WHITE)
        tv_switch_des.setTextColor(Color.WHITE)

        if (IS_OPEN) {
            tv_switch_des.text = "点击关闭电源"
            iv_battery_status.setImageResource(R.drawable.battery_work)
            rl_status.setBackgroundResource(R.drawable.bg_battery_open_has_item)
        } else {
            tv_switch_des.text = "点击开启电源"
            iv_battery_status.setImageResource(R.drawable.battery_not_work)
            rl_status.setBackgroundResource(R.drawable.bg_battery_close_has_item)
        }

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

                if (device_status == 1) {
                    openOrCloseBatter(BatteryOpenEvent(true))
                } else {
                    openOrCloseBatter(BatteryOpenEvent(false))
                }

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

                        if (1 == item.popmsg.show_msg) {
                            CommonPromptDialogHelper.promptCommonDialog(
                                activity!!,
                                "",
                                item.popmsg.msg,
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
                        if ("0" == item.device_contract.is_sign) {
                            signDialog(activity!!, item.device_contract.contract_id)
                        }

                        if ("0" == item.device_contract.contract_cg_mode) {
                            iv_signal?.visibility = View.VISIBLE
                            tv_online_status?.visibility = View.VISIBLE
                            changeElectricModelLabelImage?.visibility = View.GONE
//                            tv_unbind_battery_home?.visibility = View.VISIBLE

                            val dra = fl_switch.background as QMUIRoundButtonDrawable
                            dra.setBgData(ColorStateList.valueOf(Color.parseColor("#FF7171")))
                            changeOpenDoor?.visibility = View.GONE
                        } else {
                            iv_signal?.visibility = View.GONE
                            tv_online_status?.visibility = View.GONE
                            changeElectricModelLabelImage?.visibility = View.VISIBLE
//                            tv_unbind_battery_home?.visibility = View.GONE
                            changeOpenDoor?.visibility = View.VISIBLE
                            changeOpenDoor?.setOnClickListener {

                                val intent = Intent(activity, ScanQrCodeActivity::class.java)
                                intent.putExtra("type", "换电开门")
                                startActivityForResult(intent, 1)
                            }
                        }


                        fun initFloatLayoutItemView(
                            res: Int,
                            des: String,
                            click: () -> Unit
                        ): View {
                            val v = View.inflate(
                                activity,
                                R.layout.layout_home_battery_floatlayout_item,
                                null
                            )
                            val width =
                                (ScreenUtils.getScreenWidth() - DensityHelper.dp2px(30f)) / 5
                            val lp = v.layoutParams ?: ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            lp.width = width
                            v.layoutParams = lp
                            val iv = v.findViewById<ImageView>(R.id.iv)
                            val tv = v.findViewById<TextView>(R.id.tv)
                            iv.setImageResource(res)
                            tv.text = des
                            v.setOnClickListener { click.invoke() }
                            return v
                        }

                        qfl.apply {

                            removeAllViews()

                            addView(initFloatLayoutItemView(R.drawable.find_devices, "寻车") {
                                getPermissions(
                                    activity,
                                    PermissionType.COARSE_LOCATION,
                                    PermissionType.FINE_LOCATION,
                                    PermissionType.ACCESS_WIFI_STATE,
                                    PermissionType.ACCESS_NETWORK_STATE,
                                    allGranted = {
                                        startFgt(FgtFindBattery())
                                    })

                            })

                            addView(initFloatLayoutItemView(R.drawable.repair, "维修") {
                                startFgt(
                                    FgtRepair()
                                )
                            })

                            if ("0" == item.device_contract.contract_cg_mode) {
                                addView(
                                    initFloatLayoutItemView(
                                        R.drawable.battery_control,
                                        "更换电池"
                                    ) {
                                        //                                    startFgt(FgtControl())
                                        Log.i(
                                            "data===",
                                            "===contract_id1===${item.device_contract.contract_id}"
                                        )
                                        tryToScan(
                                            AtyScanQrcode.TYPE_CHANGE,
                                            item.device_contract.contract_id,
                                            getIsHost
                                        )
                                    })
                            } else {
                                addView(
                                    initFloatLayoutItemView(
                                        R.mipmap.change_electric_model_image,
                                        "换电"
                                    ) {
                                        //                                        Log.i("data===","===contract_id1===${item.device_contract.contract_id}")
//                                        tryToScan(AtyScanQrcode.TYPE_CHANGE, item.device_contract.contract_id,getIsHost)

                                        val intent =
                                            Intent(activity, ScanQrCodeActivity::class.java)
                                        intent.putExtra("type", "换电")
                                        intent.putExtra(
                                            "contract_id",
                                            item.device_contract.contract_id
                                        )
                                        startActivityForResult(intent, 1)

                                    })
                            }



                            addView(
                                initFloatLayoutItemView(
                                    R.drawable.battery_reback,
                                    "退还"
                                ) { startFgt(FgtReturn()) })

                            addView(initFloatLayoutItemView(R.drawable.trajectory, "轨迹") {
                                startFgt(
                                    FgtTrajectory()
                                )
                            })

                            item.credit.let { credit ->
                                if (credit.is_credit == 1) {
                                    addView(initFloatLayoutItemView(R.drawable.huankuan, "还款") {
                                        startFgt(FgtCreditReckoning.newInstance(credit.contract_id))
                                    })
                                }
                            }

                            item.device_contract.let { contract ->
                                if (contract.contract_mode == 2) {
                                    addView(
                                        initFloatLayoutItemView(
                                            R.drawable.icon_taocan_bind,
                                            "套餐绑定"
                                        ) {
                                            startFgt(FgtPackageBind.newInstance(contract.contract_id))
                                        })

                                }
                            }

                        }


                        CURRENT_DEVICEID = item.device_id.toString()
                        CURRENT_CONTRACT_ID = item.device_contract.contract_id
                        Config.getDefault().spUtils.put(KEY_LAST_DEVICE_ID, CURRENT_DEVICEID)
                        IS_OPEN = false
                        tv_battery_waring_info.text = ""

                        if (item.device_base.alert_status != 0) {
                            //错误状态
                            iv_battery_status.setImageResource(R.drawable.battery_waring)
                            tv_battery_waring_info.text = item.device_base.alert_msg
                            rl_status.setBackgroundResource(R.drawable.bg_battery_error_has_item)
                        } else {
                            // 1开2关
                            openOrCloseBatter(BatteryOpenEvent(item.device_base.device_status == "1"))
                        }





                        try {
                            val doubleRSOC: Double = item.device_base.rsoc.toDouble()

                            val batteryRes = when {
                                doubleRSOC <= 20 -> R.drawable.battery_one
                                doubleRSOC <= 40 -> R.drawable.battery_two
                                doubleRSOC <= 60 -> R.drawable.battery_three
                                doubleRSOC <= 80 -> R.drawable.battery_four
                                else -> R.drawable.battery_five
                            }

                            iv_battery.setImageResource(batteryRes)
                            tv_battery.text = "${doubleRSOC.toInt()}%"


                        } catch (e: Exception) {
                            e.printStackTrace()

                        }


                        try {
                            val signalDouble: Double = item.device_base.signallength.toDouble()

                            val signalRes = when {
                                signalDouble <= 5 -> R.drawable.signal_1
                                signalDouble in 6..15 -> R.drawable.signal_2
                                signalDouble in 16..25 -> R.drawable.signal_3
                                else -> R.drawable.signal_4
                            }

                            iv_signal.setImageResource(signalRes)


                        } catch (e: Exception) {
                            e.printStackTrace()

                        }

                        val isOnLine = item.device_base.is_online == 1

                        if (isOnLine) {
                            tv_online_status.text = "在线"
                            tv_online_status.setTextColor(Color.WHITE)
                        } else {
                            tv_online_status.text = "离线"
                            tv_online_status.setTextColor(Color.RED)
                        }


                        qfl_change_battery.setOnClickListener {
                            tryToScan(
                                AtyScanQrcode.TYPE_CHANGE, item.device_contract.contract_id,
                                getIsHost
                            )
                        }


                        tv_arrived_distance.text = "${item.device_base.use_mileage}km"

                        tv_left_distance.text = "${item.device_base.mileage}km"



                        tv_battery_num.text = "电池编号：" + item.device_id
                        tv_remark_num.text = "备注：" + item.device_contract.remark



                        tv_used_money.text = item.device_contract.total_rent_money + "元"
                        tv_ya_monety.text = item.device_contract.deposit + "元"
                        tv_rant_long.text = item.device_contract.rent_day + "月"
                        tv_avg_speed.text = item.device_base.speed_avg + "KM/H"


                        tv_total_u.text = item.device_base.totalvoltage + "V"
                        tv_rant_time.text =
                            item.device_contract.begin_time.toLong().getTime(isShowHour = false) + "至" +
                                    item.device_contract.exp_time.toLong().getTime(isShowHour = false)


                        //详细信息
                        tv_detail_standard_ah.text = item.device_base.standardcapacity + "A"
                        tv_detail_total_u.text = item.device_base.totalvoltage + "V"
                        tv_detail_total_i.text = item.device_base.electric + "A"
                        tv_detail_temperature.text = item.device_base.temperature + "摄氏度"

                        tv_detail_protect_status.text = item.device_base.protect

                        tv_detail_fet.text = item.device_base.fet
                        tv_detail_software_ver.text = item.device_base.softversion


                        tv_unbind_battery_home.setOnClickListener {
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
                }
            }


            onFail { i, s ->
                dealTwoStatus(false)

            }

            onFinish {
                srl_home?.finishRefresh()
            }
        }

    }





    private var choiceUseModelDialog: CustomDialog? = null
    private fun choiceUseModelDialog(context: Context) {
        choiceUseModelDialog = CustomDialog(context, R.layout.dialog_choice_use_model_dialog)
        choiceUseModelDialog?.gravity = Gravity.CENTER
        choiceUseModelDialog?.show()

        choiceUseModelDialog?.setOnItemClickListener(R.id.modelOne) {
            choiceUseModelDialog?.dismiss()
            Config.getDefault().spUtils.put("cg_mode", "0")
            selectDeviceType()
        }
        choiceUseModelDialog?.setOnItemClickListener(R.id.modelTwo) {
            choiceUseModelDialog?.dismiss()
            Config.getDefault().spUtils.put("cg_mode", "1")
            selectDeviceType()
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
                                requestCgOpenDoor(result!!)
                            } else {
                                val intent =
                                    Intent(activity, ChangeElectricOpenDoorActivity::class.java)
                                intent.putExtra("type", "换电")
                                intent.putExtra("contract_id", getContractId)
                                intent.putExtra("new_deviceid", result)
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

}