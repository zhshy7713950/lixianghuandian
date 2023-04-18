package com.ruimeng.things.me.bean

data class ShareInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var share_bgimg: String = "",
        var share_content: String = "", // 分享标题
        var share_ico: String = "", // 分享标题
        var share_title: String = "", // 分享标题
        var share_url: String = "", // 分享标题
        var share_url_qr: String = ""
    )
}