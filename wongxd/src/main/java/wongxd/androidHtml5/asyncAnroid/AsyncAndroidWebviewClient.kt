package wongxd.androidHtml5.asyncAnroid

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONException
import org.json.JSONObject
import wongxd.androidHtml5.androidHtml5JavaScript
import wongxd.androidHtml5.dealAndroidReqJavaScript
import wongxd.androidHtml5.reqAndroidAsyncJavaScript
import wongxd.common.bothNotNull

/**
 * Created by wongxd on 2018/12/18.
 * https://github.com/wongxd
 * wxd1@live.com
 */
class AsyncAndroidWebviewClient(val asyncMethodInterface: AsyncMethodInterface) : WebViewClient() {


    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        bothNotNull(view, request.toString()) { a, b ->
            dealUrl(a, b)
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        bothNotNull(view, url) { a, b ->
            dealUrl(a, b)
        }
        return super.shouldOverrideUrlLoading(view, url)
    }


    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.loadUrl("javascript:$reqAndroidAsyncJavaScript")
        view?.loadUrl("javascript:$androidHtml5JavaScript")
        view?.loadUrl("javascript:$dealAndroidReqJavaScript")
    }


    private fun dealUrl(view: WebView, url: String): Boolean {

        if (url.startsWith("androidhtml")) {
            val id = url.substring(url.indexOf("id=") + 3)
            var cmd: JSONObject? = null
            var arg: JSONObject? = null
            try {
                val cmdStr = AsyncJavascriptInterface.getCmdOnce(id)
                val argStr = AsyncJavascriptInterface.getArgOnce(id)
                cmd = JSONObject(cmdStr)
                arg = JSONObject(argStr)
            } catch (e: JSONException) {
                e.printStackTrace()
                return false
            }
            //另起线程处理请求
            try {


                asyncMethodInterface.apply {
                    this.id = id
                    this.cmd = cmd
                    this.arg = arg
                    this.webview = view
                }

                asyncMethodInterface.deal()

            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }



            return true
        }

        return false
    }


}