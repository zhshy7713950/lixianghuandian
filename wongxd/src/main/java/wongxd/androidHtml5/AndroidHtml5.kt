package wongxd.androidHtml5

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import android.webkit.WebView
import wongxd.androidHtml5.asyncAnroid.AsyncJavascriptInterface
import org.json.JSONObject

/**
 *
 * //https://blog.csdn.net/linmiansheng/article/details/38324013
 *
 *
 * Created by wongxd on 2018/12/18.
 * https://github.com/wongxd
 * wxd1@live.com
 */


object AndroidHtml5 {


}


/**
 * 使其支持 js  与 Android  的互调
 */
@SuppressLint("SetJavaScriptEnabled")
fun WebView.enableAsyncAndroid() {
    val webSettings = this.getSettings()

    // 设置与Js交互的权限
    webSettings.setJavaScriptEnabled(true)
    // 设置允许JS弹窗
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true)

    this.addJavascriptInterface(AsyncJavascriptInterface(), "asyncAndroid")
}


/**
 * android 同步调用 js 方法
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun WebView.callJsFun(funName: String, funParams: String = "", callback: (String) -> Unit = {}) {

    //参数1：Javascript对象名
    //参数2：Java对象名
    this.evaluateJavascript("javascript:$funName('$funParams')") { result ->
        //此处为 js 返回的结果
        callback.invoke(result)
    }
}


/**
 *   idCounter : 0,                 // 参数序列计数器
 *   OUTPUT_RESULTS : {},          // 输出的结果
 *   CALLBACK_SUCCESS : {},       // 输出的结果成功时调用的方法
 *   CALLBACK_FAIL : {},         // 输出的结果失败时调用的方法
 *
 *   下面会定义一个Iframe，Iframe会去加载我们自定义的url，以androidhtml:开头
 */
val androidHtml5JavaScript = """


    var AndroidHtml5 = {
       idCounter : 0,
       OUTPUT_RESULTS : {},
       CALLBACK_SUCCESS : {},
       CALLBACK_FAIL : {},

       callNative : function (cmd, arg, success, fail) {
              var key = "ID_" + (++ this.idCounter);

              window.asyncAndroid.setCmd(JSON.stringify(cmd), key);
              window.asyncAndroid.setArg(JSON.stringify(arg), key);

              if (typeof success != 'undefined'){
                    AndroidHtml5.CALLBACK_SUCCESS[key] = success;
              } else {
                    AndroidHtml5.CALLBACK_SUCCESS[key] = function (result){};
              }

              if (typeof fail != 'undefined'){
                    AndroidHtml5.CALLBACK_FAIL[key] = fail;
              } else {
                    AndroidHtml5.CALLBACK_FAIL[key] = function (result){};
              }


              var iframe = document.createElement("IFRAME" );
              iframe.setAttribute( "src" , "androidhtml://ready?id=" + key);
              document.documentElement.appendChild(iframe);
              iframe.parentNode.removeChild(iframe);
              iframe = null ;

              return this .OUTPUT_RESULTS[key];
       },

       callBackJs : function (result,key) {
               this .OUTPUT_RESULTS[key] = result;
               var obj = JSON.parse(result);
               var msg = obj.msg;
               var code = obj.code;
               var data = obj.data;
               if (code == 200) {
                      if (typeof this.CALLBACK_SUCCESS[key] != "undefined"){
                           var func = this.CALLBACK_SUCCESS[key];
                           setTimeout(func(data) , 0);
                     }
              } else {
                      if (typeof this.CALLBACK_FAIL[key] != "undefined") {
                           setTimeout( "AndroidHtml5.CALLBACK_FAIL['" +key+"']('" + msg + "')" , 0);
                     }
              }
       }
};




""".trim()


val reqAndroidAsyncJavaScript = """

    var reqAndroidAsync = function( action, arg, success, fail) {
      var cmd = {"service" : "wongxd","action" : action};
      AndroidHtml5.callNative(cmd, arg, success, fail);
    };

""".trim()


/**
 * 在js中定义的方法，其 格式必须为  test({"arg":"argValue"}, function(result))
 *
 *
 */
val dealAndroidReqJavaScript = """

  var dealAndroidReq = function( funcName, arg, androidCallbackId) {

     eval(
     funcName+"("+ arg +",function(res){ "
     +"window.asyncAndroid.invokeAndroidCallback( JSON.stringify(res),'"+ androidCallbackId +"')"
     +"})"
     )
   };


""".trim()

/**
 * android 异步 调用 js  方法
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun WebView.callJsFunAsync(funName: String, funParams: JSONObject = JSONObject(), callback: (String) -> Unit = {}) {

    val id = "androidCallbackId_" + System.currentTimeMillis()
    AsyncJavascriptInterface.setAndroidCallback(
        id,
        callback = object : AsyncJavascriptInterface.Companion.CallJsFunAsyncCallback {
            override fun onResult(res: String) {
                callback.invoke(res)
            }
        })

    this.loadUrl("javascript:dealAndroidReq('$funName','$funParams','$id')")

}