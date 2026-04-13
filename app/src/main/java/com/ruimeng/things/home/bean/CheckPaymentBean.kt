package com.ruimeng.things.home.bean

data class CheckPaymentBean(
    val data: Data,
    val errcode: Int,
    val errmsg: String
) {

    data class Data(
        val agentCode: String,
        val agentName: String,
        val agent_id: String,
        val begin_time: String,
        val code: String,
        val contract_id: String,
        val deposit_status: String,
        val device_id: String,
        val device_model: String,
        val exp_time: String,
        val id: String,
        val last_time: String,
        val mobile: String,
        val modelName: String,
        val paymentInfo: PaymentInfo,
        val payment_id: String,
        val realname: String,
        val rent_status: String,
        val rent_time: String,
        val user_id: String,
        val singleChangeInfo:PaymentOption,
        val open_check: Int,
        // 新增：站点电池信息相关字段
        val cabinet: Cabinet? = null
    )
    data class Cabinet(
        val siteName: String = "",
        val cellNum: String = "",
        val fullExchange: String = "1",
        val isOnline: Boolean = true
    )


}

