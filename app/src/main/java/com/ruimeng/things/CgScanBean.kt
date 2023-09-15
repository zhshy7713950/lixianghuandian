package com.ruimeng.things

import com.ruimeng.things.home.bean.PaymentOption


data class CgScanBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var exchange_tips: String = "",
        var old_info: InfoBean=InfoBean(),
        var new_info: InfoBean=InfoBean(),
    val userOptions:ArrayList<PaymentOption> = ArrayList(),
    val singleChangeInfo:PaymentOption = PaymentOption()
    ) {
        data class InfoBean(
            var device_id: String = "",
            var electricity: String = "",
            var model_str: String = ""
        )
    }
}