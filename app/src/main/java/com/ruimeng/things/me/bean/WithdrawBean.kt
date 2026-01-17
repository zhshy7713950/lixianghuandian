package com.ruimeng.things.me.bean

import java.io.Serializable

data class WithdrawListResponse(
    val data: List<WithdrawItem>
)

data class WithdrawItem(
    val id: String?,
    val created: String?,
    val balance: String?,
    val pay_status: String?, // "已支付", "支付失败", etc.
    val payInfo: PayInfo?
) : Serializable

data class PayInfo(
    val accountNum: String?,
    val accountName: String?
) : Serializable
