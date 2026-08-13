package com.ruimeng.things.voice

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.CopyOnWriteArraySet

/** Process-local visibility signal used to prevent background audio starts. */
object AppForegroundTracker : Application.ActivityLifecycleCallbacks {

    fun interface Listener {
        fun onForegroundChanged(isForeground: Boolean)
    }

    private val listeners = CopyOnWriteArraySet<Listener>()
    private var startedActivities = 0
    private var initialized = false

    @Volatile
    var isForeground: Boolean = false
        private set

    @Synchronized
    fun initialize(application: Application) {
        if (initialized) return
        initialized = true
        application.registerActivityLifecycleCallbacks(this)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun updateForeground(foreground: Boolean) {
        if (isForeground == foreground) return
        isForeground = foreground
        listeners.forEach { it.onForegroundChanged(foreground) }
    }

    override fun onActivityStarted(activity: Activity) {
        startedActivities++
        if (startedActivities == 1) updateForeground(true)
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities = (startedActivities - 1).coerceAtLeast(0)
        if (startedActivities == 0) updateForeground(false)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
