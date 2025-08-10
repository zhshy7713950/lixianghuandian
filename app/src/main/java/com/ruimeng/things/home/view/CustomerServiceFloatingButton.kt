package com.ruimeng.things.home.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ruimeng.things.FgtMain
import com.ruimeng.things.R
import com.ruimeng.things.home.CustomerServiceFragment
import com.utils.ToastHelper
import org.jetbrains.anko.dip

/**
 * 客服中心悬浮按钮
 * 固定在右下角，点击进入客服中心页面
 */
class CustomerServiceFloatingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var backgroundView: View
    private lateinit var iconView: ImageView
    private lateinit var textView: TextView

    init {
        initView()
        setupLayout()
        setupClickListener()
    }

    private fun initView() {
        // 创建背景View
        backgroundView = View(context).apply {
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dip(15).toFloat()
                setColor(context.getColor(R.color.customer_service_bg))
            }
            background = drawable
        }

        // 创建图标
        iconView = ImageView(context).apply {
            setImageResource(R.drawable.ic_online_service)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        // 创建文字
        textView = TextView(context).apply {
            text = "客服中心"
            textSize = 9f
            setTextColor(context.getColor(R.color.customer_service_text))
            gravity = Gravity.CENTER
        }

        // 添加到布局中
        addView(backgroundView)
        addView(iconView)
        addView(textView)
    }

    private fun setupLayout() {
        // 设置整体布局参数 - 移除这些设置，因为父布局已经设置了
        // layoutParams = LayoutParams(
        //     dip(50),
        //     dip(50)
        // ).apply {
        //     gravity = Gravity.BOTTOM or Gravity.END
        //     rightMargin = dip(24)
        //     bottomMargin = dip(100)
        // }

        // 设置背景View布局参数
        backgroundView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        // 设置图标布局参数
        iconView.layoutParams = LayoutParams(
            dip(25),
            dip(25)
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = dip(7)
        }

        // 设置文字布局参数
        textView.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = dip(32) // 图标高度25 + 图标上边距7 = 32
        }
    }

    private fun setupClickListener() {
        setOnClickListener {
            // 跳转到客服中心页面
            // 使用FgtMain.instance来启动Fragment，这是项目中标准的启动方式
            FgtMain.instance?.start(CustomerServiceFragment.newInstance())
                ?: ToastHelper.shortToast(context, "无法启动客服中心，请稍后重试")
        }
    }
}
