package com.ruimeng.things.home.bean

data class UserLateFeeBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var agent_id: String = "",
        var pay_type: String = "",
        var perDayFee: Float = 0f,//逾期的单日费用
        var actualLateDays: Int = 0,//已逾期的天数
        var actualLateFee: Float = 0f,//已产生的费用
    )
}