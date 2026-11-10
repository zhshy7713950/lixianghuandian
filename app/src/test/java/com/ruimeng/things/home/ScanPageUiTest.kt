package com.ruimeng.things.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanPageUiTest {
    @Test
    fun cameraParameterFailureIsHandled() {
        val updated = ScanPageUi.setLightEnabled(true) {
            throw RuntimeException("getParameters failed (empty parameters)")
        }

        assertFalse(updated)
    }

    @Test
    fun successfulCameraUpdateIsReported() {
        var requestedState: Boolean? = null
        val updated = ScanPageUi.setLightEnabled(true) { requestedState = it }

        assertTrue(updated)
        assertTrue(requestedState == true)
    }
}
