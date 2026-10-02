package com.ruimeng.things.home.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View

/** Draws the camera mask around the scan window's current position in the scrolling content. */
class ScanMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val windowPosition = IntArray(2)
    private val maskPosition = IntArray(2)
    private val frame = RectF()
    private val cornerLength = dp(30f)
    private val cornerWidth = dp(2f)
    private val scanLineHeight = dp(5f)
    private var scanWindow: View? = null

    fun follow(window: View) {
        scanWindow = window
        window.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> invalidate() }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val window = scanWindow
        if (window == null || window.width == 0 || window.height == 0) {
            canvas.drawColor(0x80000000.toInt())
            return
        }
        window.getLocationInWindow(windowPosition)
        getLocationInWindow(maskPosition)
        val left = (windowPosition[0] - maskPosition[0]).toFloat()
        val top = (windowPosition[1] - maskPosition[1]).toFloat()
        frame.set(left, top, left + window.width, top + window.height)

        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = 0x80000000.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top, paint)
        canvas.drawRect(0f, frame.top, frame.left, frame.bottom, paint)
        canvas.drawRect(frame.right, frame.top, width.toFloat(), frame.bottom, paint)
        canvas.drawRect(0f, frame.bottom, width.toFloat(), height.toFloat(), paint)

        paint.color = Color.rgb(32, 232, 174)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = cornerWidth
        val l = frame.left
        val r = frame.right
        val t = frame.top
        val b = frame.bottom
        canvas.drawLine(l, t, l + cornerLength, t, paint)
        canvas.drawLine(l, t, l, t + cornerLength, paint)
        canvas.drawLine(r - cornerLength, t, r, t, paint)
        canvas.drawLine(r, t, r, t + cornerLength, paint)
        canvas.drawLine(l, b, l + cornerLength, b, paint)
        canvas.drawLine(l, b - cornerLength, l, b, paint)
        canvas.drawLine(r - cornerLength, b, r, b, paint)
        canvas.drawLine(r, b - cornerLength, r, b, paint)

        val progress = (SystemClock.uptimeMillis() % 1800L).toFloat() / 1800f
        val lineTop = t + progress * (frame.height() - scanLineHeight)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(l, lineTop, r, lineTop,
            intArrayOf(Color.TRANSPARENT, 0xCC20E8AE.toInt(), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP)
        canvas.drawRect(l, lineTop, r, lineTop + scanLineHeight, paint)
        paint.shader = null
        if (isAttachedToWindow && b > 0 && t < height) postInvalidateDelayed(32)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
