package com.ruimeng.things.me.bean

data class DistrHomeBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var distr_all_income: String = "",//我的收益
        var distr_balance: String = "",//余额提现
        var team_num: String = ""//我的团队
        )
}