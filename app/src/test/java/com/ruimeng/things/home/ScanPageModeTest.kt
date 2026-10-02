package com.ruimeng.things.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanPageModeTest {
    @Test fun cabinetFlowShowsAllShortcuts() {
        val mode = ScanPageMode.fromLegacyType("换电", "")
        assertEquals(ScanPageMode.CABINET, mode)
        assertTrue(mode.showStation)
        assertTrue(mode.showInput)
        assertTrue(mode.showAlbum)
        assertEquals("tip-1", mode.voice)
    }

    @Test fun pickupOnlyAllowsCamera() {
        val mode = ScanPageMode.fromLegacyType("租电", "")
        assertEquals(ScanPageMode.PICKUP, mode)
        assertFalse(mode.showStation)
        assertFalse(mode.showInput)
        assertFalse(mode.showAlbum)
    }

    @Test fun couponAndOfficerHideStationChoice() {
        val coupon = ScanPageMode.fromLegacyType("", "type_ticket")
        val officer = ScanPageMode.fromLegacyType("推荐官", "")
        assertFalse(coupon.showStation)
        assertTrue(coupon.showInput)
        assertEquals("tip-5", coupon.voice)
        assertFalse(officer.showStation)
        assertTrue(officer.showAlbum)
        assertEquals(null, officer.voice)
    }
}
