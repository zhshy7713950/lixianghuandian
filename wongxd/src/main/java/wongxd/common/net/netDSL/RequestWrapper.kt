package wongxd.common.net.netDSL

import android.os.Environment
import java.io.File

/**
 * Created by wongxd on 2018/11/20.
 * https://github.com/wongxd
 * wxd1@live.com
 */
class RequestWrapper {

    var headers: MutableMap<String, String> = mutableMapOf()

    var url: String = ""

    var method: String = "POST"

    var jsonParam: String = ""

    var params: MutableMap<String, String> = mutableMapOf()

    var imgs: MutableMap<String, File> = mutableMapOf()

    var files: MutableMap<String, File> = mutableMapOf()

    var timeout: Long = 60 * 1000

    var writeTimeout: Long = 60 * 1000

    var readTimeout: Long = 60 * 1000

    var downloadPath: String = Environment.getExternalStorageDirectory().path + "/wongxd/"

    var downloadFileName: String = ""

    /**
     * 响应结果中表示 code 的字段名
     */
    var RESPONSE_CODE_NAME = "errcode"

    /**
     * 响应结果中表示 msg 的字段名
     */
    var RESPONSE_MSG_NAME = "errmsg"
    /**
     * 请求成功
     */
    var SUCUUESSED_CODE = 200

    /**
     * token失效
     */
    var TOKEN_LOST_CODE = 501

    /**
     * 当 结果 != [SUCUUESSED_CODE] 时，是否自动弹出toast
     */
    var IS_SHOW_MSG = true

    /**
     * 没有尝试把response 转成json
     */
    internal var _originResponse: (String) -> Unit = { response -> }
    internal var _success: (String) -> Unit = { response -> }
    internal var _successWithMsg: (String, String) -> Unit = { response, msg -> }
    internal var _fail: (Int, String) -> Unit = { errCode, errMsg -> }
    internal var _finish: () -> Unit = {}
    var _tokenLost: (String) -> Unit = {}
    internal var _downloadFile: (Int, Long, File?) -> Unit = { progress, total, file -> }
    internal var _uploadFile: (Int, Long, Int) -> Unit = { progress, total, index -> }

    /**
     * @param onSuccess (response)
     */
    fun onResponse(onOriginResponse: (String) -> Unit) {
        try {
            _originResponse = onOriginResponse
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param onSuccess (response)
     */
    fun onSuccess(onSuccess: (String) -> Unit) {
        try {
            _success = onSuccess
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param onSuccessWithMsg (response, msg)
     */
    fun onSuccessWithMsg(onSuccessWithMsg: (String, String) -> Unit) {
        try {
            _successWithMsg = onSuccessWithMsg
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param onError (errCode, errMsg)  errCoed ==-1  代表未成功获取服务器响应
     */
    fun onFail(onError: (Int, String) -> Unit) {
        try {
            _fail = onError
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param onFinish
     */
    fun onFinish(onFinish: () -> Unit) {
        try {
            _finish = onFinish
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param onTokenLost (msg)
     */
    fun onTokenLost(onTokenLost: (String) -> Unit) {
        try {
            _tokenLost = onTokenLost
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * @param onDownloadFile (progress, total,file) progress 为 0-99        file 在未下完时，为null
     */
    fun onDownloadFile(onDownloadFile: (Int, Long, File?) -> Unit) {
        try {
            _downloadFile = onDownloadFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param onUploadFile (progress, total,index) progress 为 0-99   index 为 多文件上传时，文件索引
     */
    fun onUploadFile(onUploadFile: (Int, Long, Int) -> Unit) {
        try {
            _uploadFile = onUploadFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}