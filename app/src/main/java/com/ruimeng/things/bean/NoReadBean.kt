package com.ruimeng.things.bean

data class NoReadBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var ver: Int = 0,
        var my: My = My(),
        var msg: Msg = Msg()
    ) {
        data class My(
            var contract_total: Int = 0,
            var contract_nocomplete: Int = 0,
            var contract_exp: Int = 0
        )

        data class Msg(var msg_noread: Int = 0)
    }
}