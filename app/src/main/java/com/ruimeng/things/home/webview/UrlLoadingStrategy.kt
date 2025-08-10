package com.ruimeng.things.home.webview

import android.webkit.WebView
import android.webkit.WebResourceRequest

/**
 * URL加载策略接口
 * 用于处理WebView的URL跳转逻辑
 */
interface UrlLoadingStrategy {
    
    /**
     * 处理URL加载
     * @param view WebView实例
     * @param request 资源请求
     * @return true表示拦截URL加载，false表示继续默认处理
     */
    fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean
    
    /**
     * 处理URL加载（兼容旧版本）
     * @param view WebView实例
     * @param url URL字符串
     * @return true表示拦截URL加载，false表示继续默认处理
     */
    fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean
}

