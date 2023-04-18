package com.ruimeng.things.me.bean


data class SharePosterListBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 200, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var id: String = "", //海报ID
        var img: String = "" //海报图片
    )
}