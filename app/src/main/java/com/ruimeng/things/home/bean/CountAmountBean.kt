package com.ruimeng.things.home.bean

class CountAmountBean (
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = ""

        ){
    data class Data(
        var totalPrice: Double = 0.0,
        var couponAmount: Double = 0.0
    )

}