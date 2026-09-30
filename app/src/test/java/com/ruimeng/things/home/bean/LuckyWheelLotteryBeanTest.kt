package com.ruimeng.things.home.bean

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import wongxd.common.toPOJO

class LuckyWheelLotteryBeanTest {

    @Test
    fun errorResponseWithArrayDataKeepsErrorWithoutCrashing() {
        val response = """{"errcode":400,"errmsg":"暂未获得抽奖资格","data":[]}"""

        val bean = response.toPOJO<LuckyWheelLotteryBean>()

        assertEquals(400, bean?.errcode)
        assertEquals("暂未获得抽奖资格", bean?.errmsg)
        assertNull(bean?.data)
    }

    @Test
    fun successResponseWithObjectDataKeepsLotteryFields() {
        val response = """{"errcode":200,"errmsg":"成功","data":{"sendCouponPrice":"10","selfCouponPrice":"20","selfCouponId":"coupon-1","paymentPrice":"30","lotteryReqNum":"request-1","nextLotteryTime":"7"}}"""

        val bean = response.toPOJO<LuckyWheelLotteryBean>()

        assertEquals(200, bean?.errcode)
        assertEquals("10", bean?.data?.sendCouponPrice)
        assertEquals("request-1", bean?.data?.lotteryReqNum)
    }

    @Test
    fun successResponseWithArrayDataIsNotTreatedAsLotteryResult() {
        val response = """{"errcode":200,"errmsg":"成功","data":[]}"""

        assertNull(parseLuckyWheelLotteryResponse(response))
    }
}
