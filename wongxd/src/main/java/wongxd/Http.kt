package wongxd

import wongxd.common.net.netDSL.BaseOkhttpHelper
import wongxd.common.net.netDSL.RequestWrapper
import wongxd.common.net.netDSL.getSign

/**
 * Created by wongxd on 2018/11/13.
 */


class Http {

    companion object {

        var host = ""

        var appId = ""
        var appKey = ""
        var token = ""

        var TOKEN_LOST_FUN: (String) -> Unit = {}
    }
}

fun http(init: RequestWrapper.() -> Unit) {

    val wrap = RequestWrapper()
    wrap.init()



    val time = (System.currentTimeMillis() / 1000).toString() + ""
    wrap.params["timeline"] = time
    wrap.params["appid"] = Http.appId
    wrap.params["token"] = Http.token
    wrap.params["appType"] = "xll"
    wrap.params["os"] = "android"
    wrap.params["sign"] = getSign(wrap.params, Http.appKey)

    wrap.url = Http.host + wrap.url
    wrap._tokenLost = Http.TOKEN_LOST_FUN


    BaseOkhttpHelper.baseOkhttp(wrap)

//    BaseHttpUrlConnectionHelper.baseHttpUrlConnection(wrap)

}