package com.ruimeng.things.bean

data class ConfigBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var push_android_appid: String = "",
        var push_ios_appid: String = "",
        var led: String = ""
    )
}