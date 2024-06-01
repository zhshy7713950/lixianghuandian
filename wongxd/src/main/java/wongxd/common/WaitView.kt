package wongxd.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntRange
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import com.wongxd.R


/**
 * Created by wongxd on 2018/09/21.
 *            https://github.com/wongxd
 *            wxd1@live.com
 *
 */

class WaitViewController private constructor(
    private val mWrapperView: View,
    private var mViewRect: RectF?,
    private var mDrawRect: RectF?
) {
    private var mOnWaitViewFilter: OnWaitViewFilter = DEFAULT_WAITVIEW_FILTER

    @ColorInt
    private var mColor = Color.parseColor("#E9E9E9")
    @Dimension
    private var mRadius: Int = 0
    @IntRange(from = 0, to = 255)
    private var mAlpha = 255

    private var mWaitView: WaitView? = null

    internal val context: Context
        get() = mWrapperView.context

    init {
        mRadius = dp2px(mWrapperView.context, 2f)
    }

    fun color(@ColorInt color: Int): WaitViewController {
        mColor = color
        return this
    }

    fun radius(@Dimension radius: Int): WaitViewController {
        mRadius = radius
        return this
    }

    fun alpha(@IntRange(from = 0, to = 255) alpha: Int): WaitViewController {
        mAlpha = alpha
        return this
    }

    fun drawRect(drawRect: RectF): WaitViewController {
        mDrawRect = drawRect
        return this
    }

    fun drawRect(width: Int, height: Int): WaitViewController {
        mDrawRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        return this
    }

    fun filter(filter: OnWaitViewFilter): WaitViewController {
        mOnWaitViewFilter = filter
        return this
    }


    fun render(): View {
        if (mWaitView == null) {
            val parent = mWrapperView.parent as ViewGroup
            mWaitView = WaitView(this)
            mWaitView!!.id = mWrapperView.id
            mWaitView!!.color(mColor).radius(mRadius).alpha(mAlpha).viewRect(mViewRect).drawRect(mDrawRect).refresh()
            val params = mWrapperView.layoutParams
            val index = parent.indexOfChild(mWrapperView)
            parent.removeView(mWrapperView)
            parent.addView(mWaitView, index, params)
        } else {
            mWaitView!!.color(mColor).radius(mRadius).alpha(mAlpha).viewRect(mViewRect).drawRect(mDrawRect).refresh()
        }
        return mWaitView!!
    }

    fun renderChilds() {
        render(mWrapperView)
    }

    private fun render(view: View) {
        val filterType = mOnWaitViewFilter.onFilter(view)
        when (filterType) {
            FilterType.Ignored -> {
            }

            FilterType.Childs -> if (view is ViewGroup) {
                val count = view.childCount
                for (i in 0 until count) {
                    val childView = view.getChildAt(i)
                    render(childView)
                }
            }

            FilterType.WaitView -> from(view) { render() }
        }
    }

    fun remove() {
        if (mWaitView == null) {
            return
        }

        val parent = mWaitView!!.parent as ViewGroup
        val params = mWaitView!!.layoutParams
        val index = parent.indexOfChild(mWaitView)
        parent.removeView(mWaitView)
        parent.addView(mWrapperView, index, params)
        mWaitView = null
    }

    fun removeChilds() {
        remove(mWrapperView)
    }

    private fun remove(view: View) {
        val filterType = mOnWaitViewFilter.onFilter(view)
        when (filterType) {
            FilterType.Ignored -> {
            }

            FilterType.Childs -> if (view is ViewGroup) {
                val count = view.childCount
                for (i in 0 until count) {
                    val childView = view.getChildAt(i)
                    remove(childView)
                }
            }

            FilterType.WaitView -> from(view) { remove() }
        }
    }

    companion object {

        private val tag_waitView_key = R.id.icon

        fun from(view: View, init: WaitViewController.() -> Unit) {
            if (view is WaitView) {
                return view.controller.init()
            }

            fun preParams(view: View, width: Int, height: Int, init: WaitViewController.() -> Unit) {
                val drawRectF = RectF(
                    view.paddingLeft.toFloat(),
                    view.paddingTop.toFloat(),
                    (width - view.paddingLeft - view.paddingRight).toFloat(),
                    (height - view.paddingTop - view.paddingBottom).toFloat()
                )

                val viewRectF = RectF(
                    0f,
                    0f,
                    (width).toFloat(),
                    (height).toFloat()
                )

                var controller: WaitViewController? = view.getTag(tag_waitView_key) as WaitViewController?
                if (controller == null) {
                    controller = WaitViewController(view, viewRectF, drawRectF)
                    view.setTag(tag_waitView_key, controller)
                }
                controller.init()
            }

            val width = view.width
            val height = view.height
            var lis: ViewTreeObserver.OnPreDrawListener? = null
            if (width == 0) {
//                val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//                view.measure(spec, spec)
//                width = view.measuredWidth
//                height = view.measuredHeight
                lis = object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        lis?.let { view.viewTreeObserver.removeOnPreDrawListener(lis) }
                        preParams(view, view.width, view.height, init)
                        return true
                    }
                }
                view.viewTreeObserver.addOnPreDrawListener(lis)
            } else
                preParams(view, width, height, init)
        }


        private val DEFAULT_WAITVIEW_FILTER = SimpleOnWaitViewFilter()


        private fun dp2px(context: Context, dp: Float): Int {
            val dm = DisplayMetrics()
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(dm)

            return (dp * dm.density + 0.5f).toInt()
        }
    }

}


@SuppressLint("ViewConstructor")
private class WaitView(val controller: WaitViewController) : View(controller.context) {

    @ColorInt
    private var mColor: Int = 0
    @Dimension
    private var mRadius: Int = 0
    @IntRange(from = 0, to = 255)
    private var mAlpha: Int = 0
    private var mViewRect: RectF? = null
    private var mDrawRect: RectF? = null
    private val mPaint: Paint = Paint()

    init {
        mPaint.isAntiAlias = true
    }


    private fun getMySize(defaultSize: Int, measureSpec: Int): Int {
        var mySize = defaultSize

        val mode = View.MeasureSpec.getMode(measureSpec)
        val size = View.MeasureSpec.getSize(measureSpec)

        when (mode) {
            MeasureSpec.UNSPECIFIED -> {
                //如果没有指定大小，就设置为默认大小
                mySize = defaultSize
            }

            MeasureSpec.AT_MOST -> {
                //如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = size

            }
            MeasureSpec.EXACTLY -> {
                // 如果是固定的大小，那就不要去改变它
                mySize = size
            }
        }

        return mySize
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val width = getMySize(40, widthMeasureSpec)
//        val height = getMySize(40, heightMeasureSpec)
//
//        setMeasuredDimension(width, height)

        setMeasuredDimension(mViewRect!!.width().toInt(), mViewRect!!.height().toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        val newX = mDrawRect!!.left - mViewRect!!.left
        val newY = mDrawRect!!.top - mViewRect!!.top
        canvas.translate(newX, newY)


        mPaint.color = mColor
        mPaint.alpha = mAlpha
        canvas.drawRoundRect(mDrawRect!!, mRadius.toFloat(), mRadius.toFloat(), mPaint)
    }


    fun color(@ColorInt color: Int): WaitView {
        mColor = color
        return this
    }

    fun radius(@Dimension radius: Int): WaitView {
        mRadius = radius
        return this
    }

    fun alpha(@IntRange(from = 0, to = 255) alpha: Int): WaitView {
        mAlpha = alpha
        return this
    }

    fun viewRect(viewRect: RectF?): WaitView {
        mViewRect = viewRect
        return this
    }

    fun drawRect(drawRect: RectF?): WaitView {
        mDrawRect = drawRect
        return this
    }

    fun refresh() {
        invalidate()
    }

}


/**
 * WaitView过滤器
 *
 */
class SimpleOnWaitViewFilter : OnWaitViewFilter {

    override fun onFilter(view: View?): FilterType {
        if (view == null) {
            return FilterType.Ignored
        }
        // 过滤不可见的
        if (view.visibility != View.VISIBLE) {
            return FilterType.Ignored
        }
        // 过滤android.view.View
        if (View::class.java == view.javaClass) {
            return FilterType.Ignored
        }

        return if (view is ViewGroup) {
            FilterType.Childs
        } else {
            FilterType.WaitView
        }
    }
}

/**
 * 过滤类型
 *
 */
enum class FilterType {
    /** 显示等待状态  */
    WaitView,
    /** 查找子View  */
    Childs,
    /** 忽略  */
    Ignored
}


/**
 * WaitView过滤器
 *
 */
interface OnWaitViewFilter {
    fun onFilter(view: View?): FilterType
}