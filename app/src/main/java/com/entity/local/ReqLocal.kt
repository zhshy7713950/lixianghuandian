package com.entity.local

data class RentStep1Local(
    val device_id: String,
    val cg_mode: String
)

data class AgentByCodeLocal(
    val code: String
)

data class UserPaymentInfoLocal(
    val user_id: String,
    val device_id: String
)