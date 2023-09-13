package com.ruimeng.things.home

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.ruimeng.things.R
import wongxd.common.checkPackage

class ChangePackagePopup(
    private val activity: Activity,
    private val listener:View.OnClickListener
) : PopupWindow(activity) {
    init {
        contentView = View.inflate(activity, R.layout.popup_map_change_package, null)
        contentView.setOnClickListener { dismiss() }
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(0x55000000))
        contentView.findViewById<TextView>(R.id.option1).setOnClickListener {
            listener.onClick(it)
            dismiss()
        }
        contentView.findViewById<TextView>(R.id.option2).setOnClickListener {
            listener.onClick(it)
            dismiss()
        }

        show(activity.window.decorView)
    }
    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.BOTTOM, 0, 0)
        }
    }
}