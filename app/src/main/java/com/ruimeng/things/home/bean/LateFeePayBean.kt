package com.ruimeng.things.home.bean

data class LateFeePayBean(
    var `data`: PayData = PayData(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class PayData(
        var alipay: Alipay = Alipay(),
        var lateFee: Float = 0f,
        var orderid: String = "",
        var wxpay: Wxpay = Wxpay()
    ) {
        data class Wxpay(
            var appId: String = "", // 1
            var nonceStr: String = "", // 5
            var packageValue: String = "", // 4
            var partnerId: String = "", // 2
            var prepayId: String = "", // 3
            var sign: String = "", // 7
            var timeStamp: String = "" // 6
        )

        data class Alipay(
            var paystr: String = "" // 1542265595
        )
    }
}