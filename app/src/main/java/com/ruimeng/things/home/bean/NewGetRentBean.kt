package com.ruimeng.things.home.bean

data class NewGetRentBean(
    val data: List<Data>,
    val errcode: Int,
    val errmsg: String
) {


    data class Data(
        val agent_id: String,
        val basePrice: String,
        val child_agents: String,
        val created: String,
        val deposit: String,
        val device_id: String,
        val discount: String,
        val end_time: Any,
        val gdiscount: String,
        val id: String,
        val is_limit: String,
        val model_id: String,
        val model_name: String,
        val modified: String,
        var options: List<Option>,
        val package_id: String,
        val package_type: String,
        val pname: String,
        val price: Int,
        val single_price: String,
        val sname: String,
        val start_time: Any,
        val time_num: String,
        val time_type: String,
        val show_start_time: String,
        val show_end_time: String,
        val agentName: String,
        val agentCode: String,
        val contract_id:String,
        val btn_return:Int = 0
    ) {

        data class Option(
            val active_time: String ="",
            val change_times: String="",
            val id: String="",
            val name: String="",
            val option_id: String="",
            val option_type: String="",
            val package_id: String="",
            val show_start_time: String ="",
            val show_end_time: String ="",
            val price: String=""
        )
    }
}