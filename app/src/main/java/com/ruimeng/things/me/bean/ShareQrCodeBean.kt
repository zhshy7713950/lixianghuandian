package com.ruimeng.things.me.bean

data class ShareQrCodeBean(
    var `data`: Data = Data(),
    var errcode: Int = 0,
    var errmsg: String = ""
) {
    data class Data(
        var recmCode: String = "",
        var qrcodeUrl: String = ""
    )
}

