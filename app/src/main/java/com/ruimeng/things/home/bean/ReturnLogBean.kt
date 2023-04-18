package com.ruimeng.things.home.bean

data class ReturnLogBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var agent_id: String = "", // 1
        var contract_id: String = "", // 0
        var created: String = "", // 1543482098
        var damage: String = "", // 0
        var damage_price: String = "", // 0.00
        var device_id: String = "", // 1025
        var handle_msg: String = "",
        var id: String = "", // 1
        var img: String = "", // http://cdn.tk.image.xianlubang.com/201811291701078473.jpg,http://cdn.tk.image.xianlubang.com/201811291701075569.jpg
        var msg: String = "", // 刚回来
        var retrun_host: String = "", // 0
        var status: String = "", // 0
        var user_id: String = "" // 11
    )
}