package com.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View

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
    var colors :IntArray? = null
    private var mDuration = 2000L
    private var mCurrentAngle = 0.0f
    private var mStartAngle = 0


    override fun onDraw(canvas: Canvas) {
        if (colors != null ){
            initPaint()
            drawOutCycle(canvas)
            drawBg(canvas)
            drawProgressBar(canvas)
        }

    }

    /**
     *  画最外的圆环
     */
    private fun drawOutCycle(canvas: Canvas){
        mCyclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCyclePaint!!.isAntiAlias = true
        mCyclePaint?.style = Paint.Style.STROKE
        mCyclePaint?.strokeWidth = 1f
        mCyclePaint?.color = Color.parseColor("#6A7682")
        canvas.drawCircle(mWidth/2f,mHeight/2f,(mWidth+mHeight)*0.25f-1,mCyclePaint!!)
    }

    private fun drawBg(canvas: Canvas){
        //画底部背景圆环
        val boarderWidth =dipToPx(20).toFloat()
        val rectBlackBg = RectF(boarderWidth, boarderWidth, (mWidth - boarderWidth), (mHeight - boarderWidth))
        mPaint!!.color = Color.parseColor("#4C5664")
        canvas.drawArc(rectBlackBg, 0f, 360f, false, mPaint!!)
    }

    private fun drawProgressBar(canvas: Canvas){
        val boarderWidth =dipToPx(20).toFloat()
        val rectBlackBg = RectF(boarderWidth, boarderWidth, (mWidth - boarderWidth), (mHeight - boarderWidth))
        //画电量进度圆环
        val section = currentCount / maxCount
        Log.i(TAG, "onDraw: "+section)
        if (section == 0.0f) {
            mPaint!!.color = Color.TRANSPARENT
        } else {
            var positions = floatArrayOf(0f,section/2.0f, section)
            if (colors!!.size == positions.size){
                val shader = SweepGradient((mWidth/2).toFloat(),(mHeight/2).toFloat(),colors!!,positions )
                var matrix = Matrix()
                matrix.setRotate(270f,(mWidth/2).toFloat(),(mHeight/2).toFloat())
                shader.setLocalMatrix(matrix)
                mPaint!!.shader = shader
            }

        }
        canvas.drawArc(rectBlackBg, 280f, mCurrentAngle, false, mPaint!!)
    }



    private fun initPaint() {
        mPaint = Paint()

        mPaint!!.isAntiAlias = true
        mPaint!!.strokeWidth = dipToPx(20).toFloat()
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.color = Color.TRANSPARENT


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
        mCurrentAngle = 360 * currentCount / 100
        setAnimator(0f,mCurrentAngle)
    }

    /**
     * 设置动画
     *
     * @param start  开始位置
     * @param target 结束位置
     */
    private fun setAnimator(start: Float, target: Float) {
        val animator: ValueAnimator = ValueAnimator.ofFloat(start, target)
        animator.setDuration(mDuration)
        animator.setTarget(mCurrentAngle)
        //动画更新监听
        animator.addUpdateListener({ valueAnimator ->
            mCurrentAngle = valueAnimator.getAnimatedValue() as Float
            invalidate()
        })
        animator.start()
    }
    fun refreshView(){
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

}