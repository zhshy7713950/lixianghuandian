package wongxd.androidHtml5.asyncAnroid

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * Created by wongxd on 2018/12/18.
 * https://github.com/wongxd
 * wxd1@live.com
 *
 *
 *
 *
 *   webview只是载体，内容的渲染需要使用webviewChromClient类去实现
 */

class AsyncWebChromeClient(
    /**
     * 暴露给外部的 打开文件选择窗的方法
     */
    private val fileChooerImp: (WebChromeClient.FileChooserParams?, Int) -> Unit = { _, _ -> }
) : WebChromeClient() {


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ): Boolean {
        uploadMessageAboveL = filePathCallback
        fileChooerImp.invoke(fileChooserParams, FILE_CHOOSER_RESULT_CODE)
        return true
    }


    private val FILE_CHOOSER_RESULT_CODE = 1995

    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null


    /**
     * 暴露给外部的 处理文件选择窗返回结果的方法
     */
    fun dealfileChooserActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        dealLogic: (data: Intent?) -> Array<Uri>
    ) {

        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return
        if (resultCode == Activity.RESULT_OK && data != null) {//从相册选择完图片
            uploadMessageAboveL?.onReceiveValue(dealLogic(data))
        } else {
            // 按返回键后  onShowFileChooser 不弹出
            uploadMessageAboveL?.onReceiveValue(null)
        }
    }


}