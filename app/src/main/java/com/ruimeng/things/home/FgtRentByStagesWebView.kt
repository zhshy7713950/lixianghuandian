package com.ruimeng.things.home

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import com.ruimeng.things.FgtMain
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_rent_by_stages_webview.*
import org.greenrobot.eventbus.EventBus
import wongxd.base.BaseBackFragment

class FgtRentByStagesWebView : BaseBackFragment() {

    companion object {
        fun newInstance(url: String, orderId: String): FgtRentByStagesWebView {
            val fragment = FgtRentByStagesWebView()
            val args = Bundle()
            args.putString("url", url)
            args.putString("orderId", orderId)
            fragment.arguments = args
            return fragment
        }
    }

    private val mUrl: String by lazy { arguments?.getString("url") ?: "" }
    private val orderId: String by lazy { arguments?.getString("orderId") ?: "" }
    private val successBaseUrl = "http://xianglilai.scxll.cn//appweb/instalmentSucc"

    override fun getLayoutRes(): Int = R.layout.fgt_rent_by_stages_webview

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "分期免息")
        initWebView()
    }

    private fun initWebView() {
        webView.apply {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.startsWith(successBaseUrl)) {
                        handlePaymentResult(url)
                        return true
                    }
                    return false
                }
            }
            loadUrl(mUrl)
        }
    }

    private fun handlePaymentResult(url: String) {
        // 解析URL参数
        val params = url.substringAfter("?")
            .split("?")
            .flatMap { it.split("&") }
            .map { it.split("=") }
            .associate { it[0] to it[1] }

        val isSuccess = params["channel"] == "bank" && 
                       params["orderid"] == orderId && 
                       params["order_sn"] != "null"

        showResultView(isSuccess)
    }

    private fun showResultView(isSuccess: Boolean) {
        // 隐藏WebView
        webView.isVisible = false
        resultLayout.isVisible = true

        // 设置结果图标和文字
        ivResult.setImageResource(
            if (isSuccess) R.drawable.ic_by_stages_pay_success
            else R.drawable.ic_by_stages_pay_fail
        )

        tvResultTitle.text = if (isSuccess) "支付成功" else "支付失败"
        tvResultDesc.text = if (isSuccess) 
            "您已完成分期免息办理，换电套餐购买成功" 
        else 
            "您未完成分期免息办理，请稍后重试"

        btnConfirm.setOnClickListener {
            if (isSuccess) {
                // 支付成功，发送事件返回首页
                pop()
                EventBus.getDefault().post(FgtMain.Companion.SwitchTabEvent(0))
            } else {
                // 支付失败，返回上一页
                pop()
            }
        }
    }
} 