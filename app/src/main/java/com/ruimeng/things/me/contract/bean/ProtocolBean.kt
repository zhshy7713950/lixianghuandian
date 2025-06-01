package com.ruimeng.things.me.contract.bean

data class ProtocolBean(
    var `data`: String = "",
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {

}