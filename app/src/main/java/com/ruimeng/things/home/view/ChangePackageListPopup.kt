package com.ruimeng.things.home.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.PaymentOption
import com.utils.TextUtil

class ChangePackageListPopup (private val activity: Activity,
                              private val options:ArrayList<PaymentOption>
) : PopupWindow(activity)  {
    init {
        contentView = View.inflate(activity, R.layout.popup_map_change_list_package, null)
        contentView.setOnClickListener { dismiss() }
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        contentView.findViewById<View>(R.id.ll_01).setOnClickListener{

        }
        setBackgroundDrawable(ColorDrawable(0x55000000))
        val option1 = options.filter { it.active_status == "1" && it.single_option == false }.first()
        if (option1 != null){
            contentView.findViewById<TextView>(R.id.tv_change_package_name1).text = "换电${option1.total_times}次"
            contentView.findViewById<TextView>(R.id.tv_change_package_times1).text = "${option1.change_times}次"
            contentView.findViewById<TextView>(R.id.tv_change_package_time1).text = "${TextUtil.formatTime(option1.start_time,option1.end_time)}"
        }
        val option2 = options.filter { it.active_status != "1"  && it.single_option == false  }.first()
        if (option2 != null){
            contentView.findViewById<TextView>(R.id.tv_change_package_name2).text = "换电${option2.total_times}次"
            contentView.findViewById<TextView>(R.id.tv_change_package_times2).text = "${option2.change_times}次"
            contentView.findViewById<TextView>(R.id.tv_change_package_time2).text = "${TextUtil.formatTime(option2.start_time,option2.end_time)}"
        }else{
            contentView.findViewById<View>(R.id.tv_title2).visibility = View.GONE
            contentView.findViewById<View>(R.id.ll_package2).visibility = View.GONE
        }

        contentView.findViewById<ImageView>(R.id.iv_close).setOnClickListener {
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