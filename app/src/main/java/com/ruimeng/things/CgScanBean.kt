package com.ruimeng.things


data class CgScanBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var exchange_tips: String = "",
        var old_info: InfoBean=InfoBean(),
        var new_info: InfoBean=InfoBean()
    ) {
        data class InfoBean(
            var device_id: String = "",
            var electricity: String = "",
            var model_str: String = ""
        )
    }
}