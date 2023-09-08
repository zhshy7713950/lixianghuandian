package com.ruimeng.things.home.bean

data class MyDevicesBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var model_str: String = "",
        var agent_id: String = "", // 0
        var begin_time: String = "", // 1542165966
        var contract_id: String = "", // 312d0f84-626c-a171-9f1a-310b6e98eb2a
        var deposit: String = "", // 100.00
        var deposit_status: String = "", // 1
        var device_id: String = "", // 1025
        var device_status: Int = 0, // 1025
        var exp_time: Int = 0, // 1542252366
        var protect : String = "",
        var id: String = "", // 1
        var is_default: String = "", // 1
        var is_host: String = "", // 1
        var remark: String = "",
        var rent_day: String = "", // 1
        var rent_money: String = "", // 100.00
        var rent_status: String = "", // 0
        var rent_time: String = "", // 1542165966
        var total_rent_money: String = "", // 100.00
        var user_id: String = "" ,// 2
        var rsoc: String = ""
    )
}