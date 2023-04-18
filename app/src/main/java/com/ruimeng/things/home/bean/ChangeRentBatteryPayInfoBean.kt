package com.ruimeng.things.home.bean

data class ChangeRentBatteryPayInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var alipay: Alipay = Alipay(),
        var orderid: String = "", // 7b26983a-a14a-a1f7-102a-c22c075cfbff
        var wxpay: Wxpay = Wxpay()
    ) {
        data class Alipay(
            var paystr: String = "" // xxx
        )

        data class Wxpay(
            var appId: String = "", // 1
            var nonceStr: String = "", // nonceStr
            var packageValue: String = "", // 4
            var partnerId: String = "", // 2
            var prepayId: String = "", // 3
            var sign: String = "", // 7
            var timeStamp: String = "" // 6
        )
    }
}