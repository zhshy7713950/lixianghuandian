package com.ruimeng.things.home.bean

data class ConfirmInstallmentBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var target: String = "", // syswebview
        var tradno: String = "", // 3c1e3f7f-627d-a8b2-b45f-81253605bd03
        var url: String = "" // http://www.baidu.com/q=8
    )
}