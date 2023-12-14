package com.ruimeng.things.home.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.MyCouponBean
import org.json.JSONObject

class ShowCouponPopup (private val activity: Activity,
                       private val coupon: JSONObject,
                       private val listener: OnClickListener
) : PopupWindow(activity){
    init {
        contentView = View.inflate(activity, R.layout.popup_show_coupon, null)
        var tv_coupon_price = contentView.findViewById<TextView>(R.id.tv_coupon_price)
        var tv_coupon_desc = contentView.findViewById<TextView>(R.id.tv_coupon_desc)
        tv_coupon_price.text = "${coupon.get("coupon_price")}"
        tv_coupon_desc.text = "租${coupon.get("limit_rent_day")}天可用"
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(0x55000000))
        contentView.findViewById<TextView>(R.id.tv_exp_remind).setOnClickListener {
            listener.onClick(it)
            dismiss()
        }
        contentView.findViewById<View>(R.id.rootView).setOnClickListener {
            listener.onClick(it)
            dismiss()
        }
        contentView.findViewById<View>(R.id.bg).setOnClickListener {
        }


    }

    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.CENTER, 0, 0)
        }
    }
}