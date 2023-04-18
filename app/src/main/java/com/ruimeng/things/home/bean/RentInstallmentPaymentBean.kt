package com.ruimeng.things.home.bean

data class RentInstallmentPaymentBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var alipay_show: Int = 0, // 1
        var bank_show: Int = 0, // 1
        var url: String = "", // http://www.baidu.com
        var wait_sec: Int = 0 // 10
    )
}