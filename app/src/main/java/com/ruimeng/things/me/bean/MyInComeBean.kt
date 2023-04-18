package com.ruimeng.things.me.bean


data class MyInComeBean(
    var `data`: Data = Data(),
    var errcode: Int = 200, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var distr_all_income: String = "", // 总收益
        var distr_balance: String = "", //可提现余额
        var list: List<LiatData> = listOf()//收益列表
    ) {
        data class LiatData(
            var explain: String = "", // 收益说明
            var balance: String = "", //收益金额
            var created: String = "" //收益时间
        )
    }
}