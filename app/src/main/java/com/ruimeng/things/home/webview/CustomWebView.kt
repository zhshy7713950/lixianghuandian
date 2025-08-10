package com.ruimeng.things.home.webview

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.graphics.Bitmap

/**
 * 自定义WebView组件
 * 使用策略模式来处理URL跳转
 */
class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {
    
    private var urlLoadingStrategy: UrlLoadingStrategy? = null
    
    init {
        initWebView()
    }
    
    /**
     * 初始化WebView设置
     */
    private fun initWebView() {
        settings.apply {
            // 启用JavaScript
            javaScriptEnabled = true
            // 启用DOM存储
            domStorageEnabled = true
            // 允许文件访问
            allowFileAccess = true
            // 设置缓存模式
            cacheMode = WebSettings.LOAD_DEFAULT
            // 启用缩放
            setSupportZoom(true)
            // 启用内置缩放控件
            builtInZoomControls = true
            // 隐藏缩放控件
            displayZoomControls = false
            // 设置默认字体大小
            defaultFontSize = 16
            // 设置最小字体大小
            minimumFontSize = 8
            // 设置混合内容模式
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        
        // 设置WebViewClient
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return urlLoadingStrategy?.shouldOverrideUrlLoading(view, request) ?: false
            }
            
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return urlLoadingStrategy?.shouldOverrideUrlLoading(view, url) ?: false
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // 可以在这里显示加载进度
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 页面加载完成
            }
        }
        
        // 设置WebChromeClient
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // 可以在这里更新进度条
            }
        }
    }
    
    /**
     * 设置URL加载策略
     * @param strategy URL加载策略
     */
    fun setUrlLoadingStrategy(strategy: UrlLoadingStrategy) {
        this.urlLoadingStrategy = strategy
    }
}

