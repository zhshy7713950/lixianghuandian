package com.ruimeng.things.home

import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.ruimeng.things.R
import com.ruimeng.things.home.webview.CustomWebView
import wongxd.base.BaseBackFragment

/**
 * 帮助中心Web页面Fragment
 * 用于显示新打开的帮助内容
 */
class HelpCenterWebFragment : BaseBackFragment() {
    
    private lateinit var webView: CustomWebView
    
    companion object {
        private const val ARG_URL = "url"
        private const val ARG_TITLE = "title"
        
        fun newInstance(url: String, title: String = "帮助中心"): HelpCenterWebFragment {
            val fragment = HelpCenterWebFragment()
            val args = Bundle()
            args.putString(ARG_URL, url)
            args.putString(ARG_TITLE, title)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun getLayoutRes(): Int = R.layout.fgt_help_center_web
    
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, arguments?.getString(ARG_TITLE) ?: "帮助中心")
        initWebView()
        loadWebContent()
    }
    
    private fun initWebView() {
        webView = rootView.findViewById(R.id.webview_help_center)
        
        // 设置URL加载策略
        webView.setUrlLoadingStrategy(object : com.ruimeng.things.home.webview.UrlLoadingStrategy {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // 在新Web中继续使用相同的策略
                val url = request?.url?.toString()
                val currentUrl = arguments?.getString(ARG_URL)
                if (url != null && url != currentUrl) {
                    // 如果URL发生变化，在当前WebView中加载
                    view?.loadUrl(url)
                    return true
                }
                return false
            }
            
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // 在新Web中继续使用相同的策略
                val currentUrl = arguments?.getString(ARG_URL)
                if (url != null && url != currentUrl) {
                    // 如果URL发生变化，在当前WebView中加载
                    view?.loadUrl(url)
                    return true
                }
                return false
            }
        })
    }
    
    private fun loadWebContent() {
        val url = arguments?.getString(ARG_URL)
        if (url != null) {
            webView.loadUrl(url)
        }
    }
    
    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.cleanup()
            webView.destroy()
        }
        super.onDestroy()
    }
}
