package com.ruimeng.things.me.bean

data class DistrCashInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var distr_balance: String = "",//可提现余额
        var is_alipay: String = "",//是否绑定支付宝1是0否
        var alipay_acct: String = "",//支付宝账号
        var is_wx: String = "",//是否绑定微信1是0否
        var wx_nickname: String = ""//微信昵称
    )
}