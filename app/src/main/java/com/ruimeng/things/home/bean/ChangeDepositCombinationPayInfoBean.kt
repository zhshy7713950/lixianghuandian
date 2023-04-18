package com.ruimeng.things.home.bean

data class ChangeDepositCombinationPayInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var alipay: Alipay = Alipay(),
        var orderid: String = "", // 9d1dcc1c-1f6c-7896-f8da-87f8d1ba14f3
        var tips: String = "", // 这是提示文字
        var wxpay: Wxpay = Wxpay()
    ) {
        data class Alipay(
            var paystr: String = "" // aaaaaaa
        )

        data class Wxpay(
            var appId: String = "", // 1
            var nonceStr: String = "", // 5
            var packageValue: String = "", // 4
            var partnerId: String = "", // 2
            var prepayId: String = "", // 3
            var sign: String = "", // 7
            var timeStamp: String = "" // 6
        )
    }
}