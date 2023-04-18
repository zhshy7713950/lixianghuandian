package wongxd.androidHtml5.asyncAnroid

import android.util.Log
import android.webkit.JavascriptInterface
import java.util.*

/**
 * Created by wongxd on 2018/12/18.
 * https://github.com/wongxd
 * wxd1@live.com
 *
 *
 *
 *
 * 注入名必须为 “ asynAndroid ”
 */
class AsyncJavascriptInterface : java.io.Serializable {

    @JavascriptInterface
    fun setCmd(cmd: String, id: String) {
        Log.d("wongxd", "cmd-$cmd  id  $id")
        CMDS[id] = cmd
    }

    @JavascriptInterface
    fun setArg(arg: String, id: String) {
        Log.d("wongxd", "args-$arg  id  $id")
        ARGS[id] = arg
    }


    @JavascriptInterface
    fun invokeAndroidCallback(res: String, id: String) {
        val androidCallback = ANDROID_CALLBACK[id]
        ANDROID_CALLBACK.remove(id)
        androidCallback?.onResult(res)

    }

    companion object {

        private val CMDS = Hashtable<String, String>()
        private val ARGS = Hashtable<String, String>()

        fun getCmdOnce(id: String): String {
            val result = CMDS[id]
            CMDS.remove(id)
            return result ?: ""
        }

        fun getArgOnce(id: String): String {
            val result = ARGS[id]
            ARGS.remove(id)
            return result ?: ""
        }


        interface CallJsFunAsyncCallback {
            fun onResult(res: String)
        }

        private val ANDROID_CALLBACK = Hashtable<String, CallJsFunAsyncCallback>()

        fun setAndroidCallback(id: String, callback: CallJsFunAsyncCallback) {
            ANDROID_CALLBACK[id] = callback
        }
    }
}
