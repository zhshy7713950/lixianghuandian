package com.ruimeng.things.me.bean

data class MyTeamBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 200, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var avatar: String = "", //用户头像
        var username: String = "", // 用户名
        var created: String = "" //注册时间
    )
}