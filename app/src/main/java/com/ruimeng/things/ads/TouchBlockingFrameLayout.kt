package com.ruimeng.things.ads

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * 可在不改变子 View 布局的前提下，截获容器内的全部触摸事件。
 */
class TouchBlockingFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var touchBlocked = false

    fun setTouchBlocked(blocked: Boolean) {
        touchBlocked = blocked
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return touchBlocked || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return touchBlocked || super.onTouchEvent(event)
    }
}
