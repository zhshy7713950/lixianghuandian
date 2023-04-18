package com.ruimeng.things.home.bean

data class RepairLogBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var agent_id: String = "", // 1
        var contract_id: String = "", // 17549
        var created: String = "", // 1543462844
        var device_id: String = "", // 1025
        var handle_agentid: String = "", // 0
        var handle_msg: String = "",
        var handle_name: String = "", // 0
        var handle_status: String = "", // 0
        var handle_time: String = "", // 0
        var id: String = "", // 2
        var msg: String = "", // 哈哈
        var tag: String = "", // 电量问题
        var user_id: String = "" // 0
    )
}