package wongxd.androidHtml5.asyncAnroid

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import org.json.JSONObject

/**
 * Created by wongxd on 2018/12/18.
 */
abstract class AsyncMethodInterface {

    companion object {
        private class UIThread protected constructor(looper: Looper) : Handler(looper) {
            companion object {
                val instance = UIThread(Looper.getMainLooper())

                fun runOnUiThread(runnable: Runnable) {
                    if (Looper.getMainLooper() == Looper.myLooper()) {
                        runnable.run()
                    } else {
                        instance.post(runnable)
                    }

                }
            }
        }
    }

    var id: String = ""
    var arg: JSONObject = JSONObject()
    var cmd: JSONObject = JSONObject()

    protected var service: String
        set(value) {}
        get() {
            return cmd.optString("service")
        }
    protected var action: String
        set(value) {}
        get() {
            return cmd.optString("action")
        }

    var webview: WebView? = null


    fun exe(isSuccessed: Boolean, msg: String = "", responseData: JSONObject) {

        UIThread.runOnUiThread(Runnable {
            val responseBody = JSONObject().apply {
                put("code", if (isSuccessed) 200 else -1)
                put("msg", msg)
                put("data", responseData)
            }
            webview?.loadUrl("javascript:AndroidHtml5.callBackJs('$responseBody','$id')")
        })
    }

    /**
     * 通过 service 和 action 去执行特定方法
     */
    abstract fun deal()
}