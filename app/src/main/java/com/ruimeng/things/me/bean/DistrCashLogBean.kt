package com.ruimeng.things.me.bean

data class DistrCashLogBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 200, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var title: String = "", // 标题
        var cash_type: String = "", // 提现类型:balance=转账,coupon=优惠券
        var pay_type: String = "", //支付类型:alipay=支付宝,wxpay微信
        var status_msg: String = "", //状态信息
        var balance: String = "", //提现金额
        var status: String = "", //提现状态:0=待审核,1=审核成功,2=审核失败
        var created: String = ""//提现时间
    )
}