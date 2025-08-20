package com.ruimeng.things.home.webview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.webkit.*
import android.graphics.Bitmap
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

/**
 * 自定义WebView组件
 * 使用策略模式来处理URL跳转和文件选择
 */
class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {
    
    private var urlLoadingStrategy: UrlLoadingStrategy? = null
    private var fileChooserStrategy: FileChooserStrategy? = null
    
    // 文件选择相关变量
    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    
    companion object {
        private const val FILE_CHOOSER_RESULT_CODE = 1001
    }
    
    init {
        initWebView()
        // 默认使用DefaultFileChooserStrategy
        if (context is Activity) {
            fileChooserStrategy = DefaultFileChooserStrategy(context as AppCompatActivity)
        }
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
            // 禁用缩放功能
            setSupportZoom(false)
            // 禁用内置缩放控件
            builtInZoomControls = false
            // 隐藏缩放控件
            displayZoomControls = false
            // 设置默认字体大小
            defaultFontSize = 16
            // 设置最小字体大小
            minimumFontSize = 8
            // 设置混合内容模式
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // 启用文件选择
            allowContentAccess = true
            // 启用自适应屏幕
            useWideViewPort = true
            loadWithOverviewMode = true
            // 设置视口宽度为设备宽度
            setInitialScale(0)
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
            
            // 处理文件选择（Android 5.0及以上）
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: WebChromeClient.FileChooserParams?
            ): Boolean {
                //暂不需要支持
//                uploadMessageAboveL = filePathCallback
//                handleFileChooserRequest(
//                    fileChooserParams?.acceptTypes?.firstOrNull(),
//                    fileChooserParams?.isCaptureEnabled?.toString()
//                )
                return true
            }
        }
    }
    
    /**
     * 处理文件选择请求
     */
    private fun handleFileChooserRequest(acceptType: String?, capture: String?) {
        android.util.Log.d("CustomWebView", "handleFileChooserRequest: acceptType=$acceptType, capture=$capture")
        
        if (fileChooserStrategy == null) {
            // 如果没有设置策略，使用默认策略
            if (context is Activity) {
                fileChooserStrategy = DefaultFileChooserStrategy(context as AppCompatActivity)
                android.util.Log.d("CustomWebView", "Created new DefaultFileChooserStrategy")
            } else {
                android.util.Log.e("CustomWebView", "Context is not an Activity")
                return
            }
        }
        
        // 如果是DefaultFileChooserStrategy，设置回调
        if (fileChooserStrategy is DefaultFileChooserStrategy) {
            val defaultStrategy = fileChooserStrategy as DefaultFileChooserStrategy
            defaultStrategy.setFileChooserCallback { uris ->
                android.util.Log.d("CustomWebView", "File chooser callback received: ${uris.size} URIs")
                if (uris.isNotEmpty()) {
                    // 处理Android 5.0以下版本
                    uploadMessage?.onReceiveValue(uris[0])
                    uploadMessage = null
                    
                    // 处理Android 5.0及以上版本
                    uploadMessageAboveL?.onReceiveValue(uris)
                    uploadMessageAboveL = null
                    
                    android.util.Log.d("CustomWebView", "Successfully sent URIs to WebView: ${uris.joinToString()}")
                } else {
                    // 用户取消选择
                    android.util.Log.d("CustomWebView", "User cancelled file selection")
                    uploadMessage?.onReceiveValue(null)
                    uploadMessageAboveL?.onReceiveValue(null)
                    uploadMessage = null
                    uploadMessageAboveL = null
                }
            }
        }
        
        // 创建模拟的FileChooserParams
        val mockParams = createMockFileChooserParams(acceptType, capture == "true")
        android.util.Log.d("CustomWebView", "Calling fileChooserStrategy.handleFileChooser")
        fileChooserStrategy?.handleFileChooser(this, mockParams, FILE_CHOOSER_RESULT_CODE)
    }
    
    /**
     * 创建模拟的FileChooserParams
     */
    private fun createMockFileChooserParams(acceptType: String?, isCapture: Boolean): WebChromeClient.FileChooserParams {
        return object : WebChromeClient.FileChooserParams() {
            override fun getMode(): Int = MODE_OPEN
            override fun getAcceptTypes(): Array<String> = arrayOf(acceptType ?: "*/*")
            override fun isCaptureEnabled(): Boolean = isCapture
            override fun getFilenameHint(): String? = null
            override fun getTitle(): CharSequence? = "选择文件"
            override fun createIntent(): Intent {
                return Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = acceptType ?: "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
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
    
    /**
     * 设置文件选择策略
     * @param strategy 文件选择策略
     */
    fun setFileChooserStrategy(strategy: FileChooserStrategy) {
        this.fileChooserStrategy = strategy
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        uploadMessage?.onReceiveValue(null)
        uploadMessageAboveL?.onReceiveValue(null)
        uploadMessage = null
        uploadMessageAboveL = null
        fileChooserStrategy?.cleanup()
    }
}

