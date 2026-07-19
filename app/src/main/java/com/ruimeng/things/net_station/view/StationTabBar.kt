package com.ruimeng.things.net_station.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 网点聚合站点的 Tab 栏。
 *
 * 由两层组成：
 * 1. 底部黑色横条（[barLayout]）：平铺若干个电柜按钮，有几个电柜就有几个按钮。
 * 2. 上层绿底高亮（[greenView]）：不可点击，比横条更高，移动到当前电柜按钮的前面。
 *
 * 宽度/文字规则：
 * - 默认每个按钮宽度 75dp，文字 "#01号柜"（两位数字）。
 * - 若 75dp * 个数 超过了"简介"整体宽度，则每个按钮宽度改为 (简介宽度 - 12dp) / 个数，
 *   同时文字只显示两位数字（如 "01"）。
 */
class StationTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val barLayout = LinearLayout(context)
    private val greenView = TextView(context)

    private var onTabSelected: ((Int) -> Unit)? = null
    private var buttonWidthPx = 0
    private var currentIndex = 0
    private var showFullText = true

    private val barHeightPx = dp(27f)
    private val greenHeightPx = dp(35f)
    private val cornerRadiusPx = dp(10f).toFloat()

    init {
        // 底部黑色横条：高度 27dp，左上/右上圆角 10dp，背景 #272D38
        barLayout.orientation = LinearLayout.HORIZONTAL
        barLayout.background = topRoundedBg(Color.parseColor("#272D38"), 0, 0)
        addView(
            barLayout,
            LayoutParams(LayoutParams.WRAP_CONTENT, barHeightPx).apply {
                gravity = Gravity.BOTTOM or Gravity.START
            }
        )

        // 上层绿底高亮：高度 35dp，左上/右上圆角 10dp，背景 #29EBB6，边框 5dp #404E59，字体 13sp 加粗
        greenView.gravity = Gravity.CENTER
        greenView.setTextColor(Color.parseColor("#131414"))
        greenView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        greenView.setTypeface(greenView.typeface, Typeface.BOLD)
        greenView.isClickable = false
        greenView.setSingleLine(true)
        greenView.background = topRoundedBg(
            Color.parseColor("#29EBB6"),
            dp(5f),
            Color.parseColor("#404E59")
        )
        addView(
            greenView,
            LayoutParams(0, greenHeightPx).apply {
                gravity = Gravity.BOTTOM or Gravity.START
            }
        )
    }

    /**
     * 设置 Tab。
     *
     * @param count 电柜个数
     * @param currentIndex 当前选中电柜下标
     * @param cardWidthPx "简介"（卡片）整体宽度，单位 px
     * @param onTabSelected 点击某个电柜按钮的回调
     */
    fun setTabs(
        count: Int,
        currentIndex: Int,
        cardWidthPx: Int,
        onTabSelected: (Int) -> Unit
    ) {
        this.onTabSelected = onTabSelected
        this.currentIndex = currentIndex.coerceIn(0, (count - 1).coerceAtLeast(0))
        barLayout.removeAllViews()

        // 计算按钮宽度与是否显示完整文字
        val defaultWidth = dp(75f)
        if (defaultWidth * count > cardWidthPx) {
            buttonWidthPx = (cardWidthPx - dp(12f)) / count
            showFullText = false
        } else {
            buttonWidthPx = defaultWidth
            showFullText = true
        }

        for (i in 0 until count) {
            val btn = TextView(context)
            btn.gravity = Gravity.CENTER
            btn.setTextColor(Color.WHITE)
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            btn.setSingleLine(true)
            btn.text = tabText(i)
            val idx = i
            btn.setOnClickListener { selectTab(idx) }
            barLayout.addView(
                btn,
                LinearLayout.LayoutParams(buttonWidthPx, LayoutParams.MATCH_PARENT)
            )
        }

        val greenParams = greenView.layoutParams as LayoutParams
        greenParams.width = buttonWidthPx
        greenView.layoutParams = greenParams
        greenView.text = tabText(this.currentIndex)
        greenView.translationX = (buttonWidthPx * this.currentIndex).toFloat()
    }

    private fun selectTab(index: Int) {
        currentIndex = index
        greenView.text = tabText(index)
        greenView.translationX = (buttonWidthPx * index).toFloat()
        onTabSelected?.invoke(index)
    }

    private fun tabText(index: Int): String {
        val twoDigit = String.format("%02d", index + 1)
        return if (showFullText) "#${twoDigit}号柜" else twoDigit
    }

    /** 生成仅左上/右上圆角的背景 */
    private fun topRoundedBg(fillColor: Int, strokeWidthPx: Int, strokeColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            cornerRadii = floatArrayOf(
                cornerRadiusPx, cornerRadiusPx, // 左上
                cornerRadiusPx, cornerRadiusPx, // 右上
                0f, 0f, // 右下
                0f, 0f  // 左下
            )
            if (strokeWidthPx > 0) setStroke(strokeWidthPx, strokeColor)
        }
    }

    private fun dp(value: Float): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()
}
