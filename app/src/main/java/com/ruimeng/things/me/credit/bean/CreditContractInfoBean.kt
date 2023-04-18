package com.ruimeng.things.me.credit.bean


/**
 * Created by wongxd on 2019/1/9.
 */
data class CreditContractInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var imgs: List<Img> = listOf(),
        var loanamount: String = "", // 3900
        var loanperiod: Int = 0, // 9
        var mobile: String = "", // 18012341234
        var monthamount: String = ""// 238
    ) {
        data class Img(
            var img: String = "" // http://cdn.tk.image.xianlubang.com/201812071425015636.png
        )
    }
}