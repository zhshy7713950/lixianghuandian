package com.ruimeng.things.net_station

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.ruimeng.things.R
import wongxd.common.checkPackage

class MapSelectPopup(
    private val activity: Activity,
    private val listener:View.OnClickListener,
    title: String = "选择应用进行导航"
) : PopupWindow(activity) {
    init {
        contentView = View.inflate(activity, R.layout.popup_map_select, null)
        var hasAmap = checkPackage(activity!!, "com.autonavi.minimap")
        var hasBaidu = checkPackage(activity!!, "com.baidu.BaiduMap")
        if (!hasAmap && !hasBaidu){
            contentView.findViewById<TextView>(R.id.tv01).text = "请先下载“高德地图” 或 “百度地图”"
        }else{
            contentView.findViewById<TextView>(R.id.tv01).text = title
        }
        contentView.findViewById<TextView>(R.id.tvAmap).visibility = if (hasAmap) View.VISIBLE else View.GONE
        contentView.findViewById<TextView>(R.id.tvBaidu).visibility = if (hasBaidu) View.VISIBLE else View.GONE

        contentView.setOnClickListener { dismiss() }
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(0x55000000))
        contentView.findViewById<TextView>(R.id.tvAmap).setOnClickListener {
            listener.onClick(it)
            dismiss()
        }
        contentView.findViewById<TextView>(R.id.tvBaidu).setOnClickListener {
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