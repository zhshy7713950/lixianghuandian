package com.ruimeng.things.home.webview

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.ruimeng.things.home.HelpCenterWebFragment
import com.ruimeng.things.FgtMain
import com.utils.ToastHelper

/**
 * 帮助中心URL加载策略
 * 当URL与目标URL不一致时，在新Web中打开
 */
class HelpCenterUrlStrategy(
    private val targetUrl: String,
    private val context: Context
) : UrlLoadingStrategy {
    
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString()
        return shouldOverrideUrlLoading(view, url)
    }
    
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if (url == null || url == targetUrl) {
            // 如果URL与目标URL一致，继续默认处理
            return false
        }
        
        // 如果URL与目标URL不一致，说明需要新开Web
        try {
            // 使用Fragment启动方式
            val fragment = HelpCenterWebFragment.newInstance(url, "帮助中心")
            FgtMain.instance?.start(fragment)
                ?: ToastHelper.shortToast(context, "无法打开新页面")
        } catch (e: Exception) {
            ToastHelper.shortToast(context, "无法打开新页面")
        }
        
        return true // 拦截URL加载
    }
}

