package com.ruimeng.things.home.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.entity.remote.Promotions
import com.ruimeng.things.FgtMain
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtCouponPurchase
import com.ruimeng.things.me.activity.AtyWeb2

class PopupHelpWindow(
    private val activity: Activity, private val event: (PopupHelpEvent) -> Unit
) : PopupWindow(activity) {

    init {
        contentView = View.inflate(activity, R.layout.popup_help_layout, null)
        val btnClose = contentView.findViewById<Button>(R.id.btnClose)
        val lySelfService = contentView.findViewById<ConstraintLayout>(R.id.ly_self_service)
        val lyOnlineService = contentView.findViewById<ConstraintLayout>(R.id.ly_online_service)
        lySelfService.setOnClickListener {
            event(PopupHelpEvent.SelfService)
            dismiss()
        }
        lyOnlineService.setOnClickListener {
            event(PopupHelpEvent.OnlineService)
            dismiss()
        }
        btnClose.setOnClickListener {
            dismiss()
        }
        setBackgroundDrawable(ColorDrawable(Color.parseColor("#99000000")))
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

sealed class PopupHelpEvent {
    object SelfService : PopupHelpEvent()
    object OnlineService : PopupHelpEvent()
}
