package wongxd.common

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import wongxd.base.AppManager

class CurrentActivityTest {
    @Before
    fun setUp() {
        AppManager.getActivityStack()?.clear()
    }

    @After
    fun clearActivityStack() {
        AppManager.getActivityStack()?.clear()
    }

    private fun newAppCompatActivityWithoutAndroidRuntime(): AppCompatActivity {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val unsafe = unsafeField.get(null)
        return unsafeClass.getMethod("allocateInstance", Class::class.java)
            .invoke(unsafe, AppCompatActivity::class.java) as AppCompatActivity
    }

    @Test
    fun thirdPartyActivityOnTopDoesNotCauseClassCastException() {
        val host = newAppCompatActivityWithoutAndroidRuntime()
        AppManager.getAppManager().addActivity(host)
        AppManager.getAppManager().addActivity(Activity())

        assertSame(host, getCurrentAty())
    }
}
