package com.ruimeng.things.home.bean

import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import com.utils.EmptyArrayAsNullTypeAdapterFactory
import wongxd.common.gson

data class LuckyWheelLotteryBean(
    @JsonAdapter(EmptyArrayAsNullTypeAdapterFactory::class)
    var `data`: Data? = null,
    var errcode: Int = 0,
    var errmsg: String = ""
) {
    data class Data(
        var sendCouponPrice: String = "",
        var selfCouponPrice: String = "",
        var selfCouponId: String = "",
        var paymentPrice: String = "",
        var lotteryReqNum: String = "",
        var nextLotteryTime: String = ""
    )
}

fun parseLuckyWheelLotteryResponse(response: String): LuckyWheelLotteryBean? {
    return try {
        val bean = gson.fromJson(response, LuckyWheelLotteryBean::class.java) ?: return null
        bean.takeIf { it.errcode != 200 || it.data != null }
    } catch (_: JsonParseException) {
        null
    }
}
