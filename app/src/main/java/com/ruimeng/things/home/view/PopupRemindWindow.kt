package com.ruimeng.things.home.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.view.isVisible
import com.ruimeng.things.R

class PopupRemindWindow (private val activity: Activity
) : PopupWindow(activity) {

    init {
        contentView = View.inflate(activity, R.layout.popup_ad_layout, null)
        val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
        val ivContent = contentView.findViewById<ImageView>(R.id.ivContent)
        ivContent.setImageResource(R.drawable.ic_home_remind)
        ivContent.isVisible = true
        ivClose.setOnClickListener {
            dismiss()
        }
        setBackgroundDrawable(ColorDrawable(Color.parseColor("#4A000000")))
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        isClippingEnabled = false
    }

    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.CENTER, 0, 0)
        }
    }
}