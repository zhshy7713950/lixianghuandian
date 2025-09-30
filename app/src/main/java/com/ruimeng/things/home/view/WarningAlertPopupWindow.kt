package com.ruimeng.things.home.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.ruimeng.things.R
import wongxd.base.FgtBase

/**
 * 通用警告提醒弹窗
 * 用于显示各种警告信息，支持自定义原因说明和处理办法
 * 
 * @param fgtBase Fragment基类实例
 * @param reason 原因说明文本
 * @param solution 处理办法文本
 */
class WarningAlertPopupWindow(
    private val fgtBase: FgtBase,
    private val reason: String,
    private val solution: String
) : PopupWindow(fgtBase.requireActivity()) {

    init {
        val context = fgtBase.requireContext()
        contentView = View.inflate(context, R.layout.popup_warning_alert, null)
        
        // 初始化视图组件
        val tvReason = contentView.findViewById<TextView>(R.id.tvReason)
        val tvSolution = contentView.findViewById<TextView>(R.id.tvSolution)
        val btnOk = contentView.findViewById<com.flyco.roundview.RoundTextView>(R.id.btnOk)
        
        // 设置文本内容
        tvReason.text = reason
        tvSolution.text = solution
        
        // 设置按钮点击事件
        btnOk.setOnClickListener {
            dismiss()
        }
        
        // 设置弹窗属性
        setBackgroundDrawable(ColorDrawable(Color.parseColor("#4A000000"))) // 30%透明度的黑色背景
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        isClippingEnabled = false
    }

    /**
     * 显示弹窗
     * @param view 锚点视图
     */
    fun show(view: View) {
        if (fgtBase.requireActivity().window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.CENTER, 0, 0)
        }
    }
}
