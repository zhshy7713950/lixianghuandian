package com.ruimeng.things.home.bean

data class LuckyWheelLotteryBean(
    var `data`: Data = Data(),
    var errcode: Int = 0,
    var errmsg: String = ""
) {
    data class Data(
        var sendCouponPrice: String = "",
        var selfCouponPrice: String = "",
        var selfCouponId: String = "",
        var paymentPrice: String = "",
        var lotteryReqNum: String = "",
        var nextLotteryTime: String = ""
    )
}

