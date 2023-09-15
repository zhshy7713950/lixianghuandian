package com.ruimeng.things.home.bean

class PaymentInfo (
    val agent_id: String = "",
    val basePrice: String = "",
    val child_agents: String = "",
    val created: String = "",
    val deposit: String = "",
    val device_id: String= "",
    val discount: String= "",
    val end_time: String= "",
    val gdiscount: String= "",
    val id: String= "",
    val is_limit: String= "",
    val model_id: String= "",
    var model_name: String= "",
    val modelName: String= "",
    val modified: String= "",
    var options: List<PaymentOption> = ArrayList(),
    var userOptions: List<PaymentOption> = ArrayList(),
    val package_id: String= "",
    val package_type: String= "",
    val pname: String= "",
    val price: Int= 0,
    val single_price: String= "",
    val sname: String,
    val start_time: Any= "",
    val time_num: String= "",
    val time_type: String= "",
    val show_start_time: String= "",
    val show_end_time: String= "",
    val agentName: String= "",
    val paymentName: String= "",
    val exp_time: String= "",
    val agentCode: String= "",
    val contract_id:String= "",
    val btn_return:Int = 0
        )
{

}