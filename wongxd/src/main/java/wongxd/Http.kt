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
    wrap.params.put("timeline", time)
    wrap.params.put("appid", Http.appId)
    wrap.params.put("token", Http.token)
    wrap.params.put("sign", getSign(wrap.params, Http.appKey))
    wrap.params.put("appType", "lxhd")
    wrap.params.put("os", "android")
    wrap.jsonParam.put("timeline", time)
    wrap.jsonParam.put("appid", Http.appId)
    wrap.jsonParam.put("token", Http.token)
    wrap.jsonParam.put("sign", getSign(wrap.params, Http.appKey))
    wrap.jsonParam.put("appType", "lxhd")
//    wrap.jsonParam.put("os","Android")

    wrap.url = Http.host + wrap.url
    wrap._tokenLost = Http.TOKEN_LOST_FUN


    BaseOkhttpHelper.baseOkhttp(wrap)

//    BaseHttpUrlConnectionHelper.baseHttpUrlConnection(wrap)

}