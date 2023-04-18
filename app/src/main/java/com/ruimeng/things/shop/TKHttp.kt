package   com.ruimeng.things.shop

import wongxd.common.net.netDSL.BaseHttpUrlConnectionHelper
import wongxd.common.net.netDSL.RequestWrapper
import wongxd.common.net.netDSL.getSign

/**
 * Created by wongxd on 2018/11/13.
 */


class TkHttp {

    companion object {

        var host = TkPath.tkHost

        var appId = "822"
        var appKey = "eb8ec6b3e7a3afbc54962db73e86bdec "
        var token = ""

        var TOKEN_LOST_FUN: (String) -> Unit = {}
    }
}

fun tkHttp(init: RequestWrapper.() -> Unit) {

    val wrap = RequestWrapper()
    wrap.init()


    val time = (System.currentTimeMillis() / 1000).toString() + ""
    wrap.params.put("timeline", time)
    wrap.params.put("appid", TkHttp.appId)
    wrap.params.put("token", TkHttp.token)
    wrap.params.put("sign", getSign(wrap.params, TkHttp.appKey))

    wrap.url = TkHttp.host + wrap.url
    wrap._tokenLost = TkHttp.TOKEN_LOST_FUN


//    BaseOkhttpHelper.baseOkhttp(wrap)

    BaseHttpUrlConnectionHelper.baseHttpUrlConnection(wrap)

}