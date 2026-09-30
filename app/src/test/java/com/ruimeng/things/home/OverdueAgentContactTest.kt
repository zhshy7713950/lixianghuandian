package com.ruimeng.things.home

import com.google.gson.Gson
import com.ruimeng.things.home.bean.PaymentDetailBean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OverdueAgentContactTest {

    @Test
    fun overdueEnterpriseUserUsesAgentMobileFromPaymentResponse() {
        val response = """{"data":{"agentMobile":"13800138000"}}"""
        val payment = Gson().fromJson(response, PaymentDetailBean::class.java)

        assertEquals("13800138000", overdueAgentPhone(3, payment.data.agentMobile))
    }

    @Test
    fun overdueWithoutAgentMobileDoesNotShowContact() {
        val response = """{"data":{}}"""
        val payment = Gson().fromJson(response, PaymentDetailBean::class.java)

        assertNull(overdueAgentPhone(3, payment.data.agentMobile))
        assertNull(overdueAgentPhone(3, "   "))
    }

    @Test
    fun agentMobileDoesNotShowContactOutsideOverdueState() {
        assertNull(overdueAgentPhone(2, "13800138000"))
    }
}
