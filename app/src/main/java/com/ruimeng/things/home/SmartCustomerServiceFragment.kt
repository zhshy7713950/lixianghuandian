package com.ruimeng.things.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.ruimeng.things.R
import com.ruimeng.things.home.webview.CustomWebView
import com.ruimeng.things.home.webview.HelpCenterUrlStrategy
import wongxd.base.BaseBackFragment

/**
 * 智能客服页面Fragment
 * 用于显示微信公众号智能客服聊天界面
 */
class SmartCustomerServiceFragment : BaseBackFragment() {
    
    private lateinit var webView: CustomWebView
    
    companion object {
        private const val SMART_CUSTOMER_SERVICE_URL = "https://chatbot.weixin.qq.com/webapp/LhlReioO3rdwgUNbBS3uKFmac3Dmi9?robotName=%E9%94%82%E4%BA%AB%E6%8D%A2%E7%94%B5%E6%99%BA%E8%83%BD%E5%AE%A2%E6%9C%8D"
        
        fun newInstance(): SmartCustomerServiceFragment {
            return SmartCustomerServiceFragment()
        }
    }
    
    override fun getLayoutRes(): Int = R.layout.fgt_smart_customer_service
    
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "智能客服")
        setupTopbarActions()
        initWebView()
        loadWebContent()
    }
    
    private fun setupTopbarActions() {
        // 设置人工客服按钮点击事件
        val button = topbar.addRightTextButton("人工客服", R.id.action_manual_service)
        button.setTextColor(android.graphics.Color.WHITE)
        button.setOnClickListener {
            showManualServiceDialog()
        }
    }
    
    private fun showManualServiceDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("人工客服")
            .setMessage("您是否需要咨询微信端人工客服？")
            .setPositiveButton("确定") { _, _ ->
                openManualServiceInBrowser()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun openManualServiceInBrowser() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://work.weixin.qq.com/kfid/kfce11eb6a47338f200"))
            startActivity(intent)
        } catch (e: Exception) {
            // 如果无法打开系统浏览器，显示错误提示
            AlertDialog.Builder(requireContext())
                .setTitle("提示")
                .setMessage("无法打开系统浏览器，请手动复制链接到浏览器中打开")
                .setPositiveButton("确定", null)
                .show()
        }
    }
    
    private fun initWebView() {
        webView = rootView.findViewById(R.id.webview_smart_customer_service)
        
        // 使用HelpCenterUrlStrategy处理URL拦截
        // 当点击蓝色超链接时，新开Web页面，标题固定为"帮助中心"
        val strategy = HelpCenterUrlStrategy(SMART_CUSTOMER_SERVICE_URL, requireContext())
        webView.setUrlLoadingStrategy(strategy)
    }
    
    private fun loadWebContent() {
        webView.loadUrl(SMART_CUSTOMER_SERVICE_URL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleFileChooserResult(requestCode, resultCode, data)
    }

    /**
     * 处理文件选择结果
     * 如果H5页面中有文件选择功能，需要调用此方法
     */
    private fun handleFileChooserResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (::webView.isInitialized) {
            webView.handleFileChooserResult(requestCode, resultCode, data)
        }
    }
    
    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
            webView.cleanup()
        }
        super.onDestroy()
    }
}
