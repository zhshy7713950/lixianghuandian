package com.utils.quicklogin

import android.app.Activity
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import wongxd.base.AppManager

class QuickLoginHelperTest {
    @After
    fun clearActivityStack() {
        AppManager.getActivityStack()?.clear()
    }

    @Test
    fun emptyActivityStackDoesNotCrashQuickLogin() {
        val activity = Activity()
        val manager = AppManager.getAppManager()
        manager.addActivity(activity)
        manager.removeActivity(activity)

        assertNull(QuickLoginHelper.currentActivityOrNull())
    }

    @Test
    fun existingActivityIsStillUsed() {
        val activity = Activity()
        AppManager.getAppManager().addActivity(activity)

        assertSame(activity, QuickLoginHelper.currentActivityOrNull())
    }
}
