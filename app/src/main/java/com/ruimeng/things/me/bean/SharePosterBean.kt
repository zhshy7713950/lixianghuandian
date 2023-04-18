package com.ruimeng.things.me.bean


data class SharePosterBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var img_url: String = "",//分享图片
        var reg_url: String = ""//注册地址
    )
}