package com.ruimeng.things.home

enum class ScanPageMode(val prompt: String, val voice: String?, val showStation: Boolean, val showInput: Boolean, val showAlbum: Boolean) {
    CABINET("请扫描电柜二维码", "tip-1", true, true, true),
    PICKUP("请扫描电柜二维码", "tip-1", false, false, false),
    COUPON("请扫描兑换券二维码", "tip-5", false, true, true),
    OFFICER("请扫描推荐官二维码", null, false, true, true);

    companion object {
        fun fromLegacyType(type: String, prefix: String): ScanPageMode = when {
            prefix == AtyScanQrcode.TYPE_TICKET -> COUPON
            type == "推荐官" -> OFFICER
            type == "租电" -> PICKUP
            else -> CABINET
        }
    }
}
