package com.view

import android.content.Context
import android.graphics.*
import kotlin.jvm.JvmOverloads
import com.view.ProgressView
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec

/***
 * 自定义圆弧进度条
 *
 * @author liujing
 */
class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    /***
     * 设置最大的进度值
     *
     * @param maxCount
     */
    var maxCount = 0f
    private var currentCount = 0f
    var score = 0
    private var mPaint: Paint? = null
    private var mCyclePaint: Paint? = null
    private var mWidth = 0
    private var mHeight = 0
    var textColor:Int = Color.parseColor("#29EBB6")
    var showAlter :Boolean = false
    var colors :IntArray? = null
    private fun init(context: Context) {
        mPaint = Paint()
        mCyclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (colors != null){
            initPaint()
            //画最外的圆环
            canvas.drawCircle(mWidth/2f,mHeight/2f,(mWidth+mHeight)*0.25f-1,mCyclePaint!!)
            //画底部背景圆环
            val boarderWidth =dipToPx(20).toFloat()
            val rectBlackBg = RectF(boarderWidth, boarderWidth, (mWidth - boarderWidth), (mHeight - boarderWidth))
            mPaint!!.color = Color.parseColor("#4C5664")
            canvas.drawArc(rectBlackBg, 0f, 360f, false, mPaint!!)


            //画电量进度圆环
            val section = currentCount / maxCount
            Log.i(TAG, "onDraw: "+section)
            if (section == 0.0f) {
                mPaint!!.color = Color.TRANSPARENT
            } else {
                val shader = SweepGradient((mWidth/2).toFloat(),(mHeight/2).toFloat(),colors!!, floatArrayOf(0f, section))
                var matrix = Matrix()
                matrix.setRotate(270f,(mWidth/2).toFloat(),(mHeight/2).toFloat())
                shader.setLocalMatrix(matrix)
                mPaint!!.shader = shader
            }
            canvas.drawArc(rectBlackBg, 280f, section * 360, false, mPaint!!)
        }

    }

    private fun initPaint() {
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeWidth = dipToPx(20).toFloat()
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.color = Color.TRANSPARENT

        mCyclePaint!!.isAntiAlias = true
        mCyclePaint?.style = Paint.Style.STROKE
        mCyclePaint?.strokeWidth = 1f
        mCyclePaint?.color = Color.parseColor("#6A7682")
    }

    private fun dipToPx(dip: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dip * scale + 0.5f * if (dip >= 0) 1 else -1).toInt()
    }

    fun getCurrentCount(): Float {
        return currentCount
    }

    /***
     * 设置当前的进度值
     *
     * @param currentCount
     */
    fun setCurrentCount(currentCount: Float) {
        this.currentCount = if (currentCount > maxCount) maxCount else currentCount
        this.score = currentCount.toInt()
        invalidate()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = if (widthSpecMode == MeasureSpec.EXACTLY
            || widthSpecMode == MeasureSpec.AT_MOST
        ) {
            widthSpecSize
        } else {
            0
        }
        mHeight = if (heightSpecMode == MeasureSpec.AT_MOST
            || heightSpecMode == MeasureSpec.UNSPECIFIED
        ) {
            dipToPx(15)
        } else {
            heightSpecSize
        }
        setMeasuredDimension(mWidth, mHeight)
    }

    companion object {

        private val TAG = "ProgressView"
    }


    init {
        init(context)
    }
}