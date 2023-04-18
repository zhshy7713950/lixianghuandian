package com.ruimeng.things.home.bean

data class GetPayByDepositBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var alipay: Alipay = Alipay(),
        var orderid: String = "", // f2a43165-e23c-c925-6b52-2169327255e0
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
            var paystr: String = "" // 1542165966
        )
    }
}