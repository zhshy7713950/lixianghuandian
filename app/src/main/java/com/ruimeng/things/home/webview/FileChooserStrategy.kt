package com.ruimeng.things.home.webview

import android.webkit.WebView
import android.webkit.WebChromeClient

/**
 * 文件选择策略接口
 * 使用策略模式来处理不同类型的文件选择逻辑
 */
interface FileChooserStrategy {
    
    /**
     * 处理文件选择请求
     * @param webView WebView实例
     * @param fileChooserParams 文件选择参数
     * @param requestCode 请求码
     * @return 是否处理了文件选择请求
     */
    fun handleFileChooser(
        webView: WebView?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
        requestCode: Int
    ): Boolean
    
    /**
     * 处理文件选择结果
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的数据
     * @return 是否成功处理了结果
     */
    fun handleFileChooserResult(
        requestCode: Int,
        resultCode: Int,
        data: android.content.Intent?
    ): Boolean
    
    /**
     * 获取支持的文件类型
     * @return 支持的文件类型列表
     */
    fun getSupportedFileTypes(): List<String>
    
    /**
     * 检查是否支持指定的文件类型
     * @param mimeType MIME类型
     * @return 是否支持
     */
    fun isFileTypeSupported(mimeType: String): Boolean
    
    /**
     * 清理资源
     * 在策略不再使用时调用，防止内存泄漏
     */
    fun cleanup()
}
