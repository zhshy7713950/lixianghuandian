package com.entity.local

import wongxd.Config

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

data class GetMapKey(
    val os: String = "android",
    val `package`: String = Config.getDefault().packageName
)