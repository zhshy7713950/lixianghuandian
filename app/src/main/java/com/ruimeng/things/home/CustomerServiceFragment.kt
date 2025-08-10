package com.ruimeng.things.home

import android.content.Intent
import android.os.Bundle
import com.ruimeng.things.FgtMain
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.R
import com.ruimeng.things.ScanQrCodeActivity
import com.ruimeng.things.home.vm.CustomerServiceViewModel
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.fgt_customer_service.*
import me.yokeyword.fragmentation.SupportFragment
import wongxd.base.BaseBackFragment
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import org.greenrobot.eventbus.EventBus
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.uuzuche.lib_zxing.activity.CodeUtils
import wongxd.common.getCurrentAty
import androidx.fragment.app.viewModels
import com.net.NetworkResponse
import com.ruimeng.things.home.webview.HelpCenterUrlStrategy
import com.ruimeng.things.home.SmartCustomerServiceFragment
import com.ruimeng.things.utils.PageNavigationHelper

/**
 * 客服中心页面
 */
class CustomerServiceFragment : BaseBackFragment() {

    companion object {
        private const val HELP_CENTER_URL = "https://xianglilai.scxll.cn/appH5/SC-HELPCENTER.html"
        private const val SCAN_QR_REQUEST_CODE = 1001
        
        fun newInstance(): CustomerServiceFragment {
            return CustomerServiceFragment()
        }
    }

    private val vm: CustomerServiceViewModel by viewModels()

    override fun getLayoutRes(): Int = R.layout.fgt_customer_service

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "客服中心")
        setupListeners()
        loadHelpCenter()
    }

    private fun setupListeners() {
        // 电池卡仓
        ll_battery_compartment.setOnClickListener {
            handleBatteryCompartment()
        }
        
        // 切换电池
        ll_switch_battery.setOnClickListener {
            // 进入首页-切换电池页面
            PageNavigationHelper.safeStartFragment(FgtSwitchBattery(), this)
        }
        
        // 变更手机
        ll_change_mobile.setOnClickListener {
            // 进入我的-变更手机号码页面
            PageNavigationHelper.safeStartFragment(FgtChangeMobile.newInstance(FgtChangeMobile.VERIFY_TYPE), this)
        }
        
                    // 优惠活动
            ll_promotional_activities.setOnClickListener {
                // 返回到主页面并切换到优惠活动tab
                PageNavigationHelper.backToMainAndSwitchTab(2, this)
            }
        
        // 在线客服
        btn_online_service.setOnClickListener {
            // 跳转到智能客服页面
            PageNavigationHelper.safeStartFragment(SmartCustomerServiceFragment.newInstance(), this)
        }
        
        // 客服热线
        btn_service_hotline.setOnClickListener {
            showCustomerServiceHotlineDialog()
        }
    }

    private fun loadHelpCenter() {
        // 设置URL加载策略
        val strategy = HelpCenterUrlStrategy(HELP_CENTER_URL, requireContext())
        webview_help_center.setUrlLoadingStrategy(strategy)
        
        // 加载帮助中心H5页面
        webview_help_center.loadUrl(HELP_CENTER_URL)
    }

    private fun handleBatteryCompartment() {
        // 判断电池编号!=空、电池编号!=虚拟号、套餐生效状态!=已过期、套餐生效状态!=已冻结
        val userInfo = InfoViewModel.getDefault().userInfo.value
        if (userInfo == null) {
            ToastHelper.shortToast(activity, "用户信息获取失败")
            return
        }
        
        // 获取电池信息，判断条件
        val deviceId = FgtHome.CURRENT_DEVICEID
        if (deviceId.isNullOrEmpty() || deviceId == "0") {
            ToastHelper.shortToast(activity, "没有需要取回的电池")
            return
        }
        
        // 使用ViewModel获取电池状态信息
        vm.getUserPaymentInfo("${userInfo.id}", deviceId).observe(this) { response ->
            when (response) {
                is NetworkResponse.Success -> {
                    val paymentInfo = response.data.data
                    if (paymentInfo == null) {
                        ToastHelper.shortToast(activity, "获取电池信息失败")
                        return@observe
                    }
                    
                    val activeStatus = paymentInfo.active_status
                    val isVirtual = deviceId.startsWith("8") && deviceId.length == 8
                    
                    // 判断条件：电池编号!=空、电池编号!=虚拟号、套餐生效状态!=已过期、套餐生效状态!=已冻结
                    if (isVirtual || activeStatus == "2" || activeStatus == "3") {
                        ToastHelper.shortToast(activity, "没有需要取回的电池")
                        return@observe
                    }
                    
                    // 条件满足，跳转扫码页面
                    ToastHelper.shortToast(activity, "请扫描电柜二维码")
                    getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                        val intent = Intent(activity, ScanQrCodeActivity::class.java)
                        intent.putExtra("type", "自助开仓")
                        startActivityForResult(intent, SCAN_QR_REQUEST_CODE)
                    })
                }
                is NetworkResponse.BizError -> {
                    ToastHelper.shortToast(activity, "获取电池信息失败: ${response.errorMessage}")
                }
                is NetworkResponse.UnknownError -> {
                    ToastHelper.shortToast(activity, "获取电池信息失败: ${response.errorMessage}")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == SCAN_QR_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            data?.let { intent ->
                val bundle = intent.extras
                if (bundle != null) {
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) { // 扫码成功
                        val result = bundle.getString(CodeUtils.RESULT_STRING)
                        val getType = bundle.getString("type")
                        
                        if ("自助开仓" == getType) {
                            result?.let { code ->
                                // 调用自助开仓接口
                                selfService(code)
                            }
                        }
                    } else {
                        ToastHelper.shortToast(activity, "解析二维码失败")
                    }
                }
            }
        }
    }

    private fun selfService(code: String) {
        val deviceId = FgtHome.CURRENT_DEVICEID
        if (deviceId.isNullOrEmpty()) {
            ToastHelper.shortToast(activity, "电池信息获取失败")
            return
        }
        
        // 使用ViewModel调用接口
        vm.changeError(deviceId, code).observe(this) { msg ->
            ToastHelper.shortToast(activity, msg)
        }
    }
    
    private fun showCustomerServiceHotlineDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("客服热线")
            .setMessage("4000283969")
            .setNegativeButton("取消", null)
            .setPositiveButton("确认") { _, _ ->
                // 跳转到拨号界面
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = android.net.Uri.parse("tel:4000283969")
                startActivity(intent)
            }
            .show()
    }
    
    private fun startFgt(toFgt: SupportFragment) {
        // 使用FgtMain.instance来启动Fragment，这是项目中标准的启动方式
        FgtMain.instance?.start(toFgt)
            ?: ToastHelper.shortToast(activity, "无法启动页面，请稍后重试")
    }
}
