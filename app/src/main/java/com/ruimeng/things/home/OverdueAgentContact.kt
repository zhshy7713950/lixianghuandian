package com.ruimeng.things.home

internal fun overdueAgentPhone(deviceStatus: Int, agentMobile: String?): String? =
    agentMobile?.trim()?.takeIf { deviceStatus == 3 && it.isNotEmpty() }
