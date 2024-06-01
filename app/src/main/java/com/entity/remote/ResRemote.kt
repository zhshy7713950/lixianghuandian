package com.entity.remote

data class RentStep1Remote(
    val status: Int?
)

data class AgentInfoRemote(
    val agent_id: String?,
    val agentName: String?,
    val code: String?
)

data class UserPaymentInfoRemote(
    val active_status: String?,//“套餐”的生效状态：1-生效中，2-未生效(未购买、已过期），3-已冻结
    val deposit_status: String?,//“押金”是否已交：0-未交，1-已交
    val rent_status: String?//“租金”是否已交：0-未交，1-已交
)