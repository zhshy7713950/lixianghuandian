package com.ruimeng.things.me.bean

data class TruenameInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var idcard: String = "",
        var img1: String = "",
        var img2: String = "",
        var name: String = "",
        var status: Int = 0 // 0  0未提交审核 1 审核中 2审核失败 3审核成功

    )
}