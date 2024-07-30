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

data class GetMapKeyLocal(
    val os: String = "android",
    val `package`: String = Config.getDefault().packageName
)

data class OneKeyLoginLocal(
    val wangyiToken: String,
    val accessToken: String
)

data class GetAdInfoLocal(
    val userId: String,
    val lat: String,
    val lng: String
)

data class AdPayLocal(
    val userId: String,
    val packageId: String,
    val payType: String,
    val price: String
)

data class ServerPayResultLocal(
    val orderid: String
)
data class ChangeErrorLocal(
    val deviceId: String,
    val code: String
)