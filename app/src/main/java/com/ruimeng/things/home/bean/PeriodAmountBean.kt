package com.ruimeng.things.home.bean

class PeriodAmountBean (
    var `data`: MutableList<Data> = mutableListOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = ""

        ){
    data class Data(
        var period: Int = 0,
        var periodAmount: Double = 0.0
    )

}