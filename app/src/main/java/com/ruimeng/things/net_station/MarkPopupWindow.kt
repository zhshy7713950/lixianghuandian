package com.ruimeng.things.net_station

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.utils.SystemUtils

class MarkPopupWindow(
    private val activity: Activity,
    private val agent: NetStationBean.Data.X,
    private val type:String,
    private val markCallback: OnMarKCallback

) : PopupWindow(activity) {
    init {
        val v = View.inflate(activity, R.layout.layout_station_infowindow, null)
        val ivClose = v.findViewById<ImageView>(R.id.iv_close)
        val tvStationName = v.findViewById<TextView>(R.id.tv_station_name)
        val tvStationPhone = v.findViewById<TextView>(R.id.tv_station_phone)
        val tvStationDistance = v.findViewById<TextView>(R.id.tv_station_distance)
        val tvStationLocation = v.findViewById<TextView>(R.id.tv_station_location)
        val tvStationTag = v.findViewById<TextView>(R.id.tv_station_tag)
        val tvStationBatteryCount = v.findViewById<TextView>(R.id.tv_station_battery_count)


        contentView = v
        contentView.setOnClickListener { dismiss() }
        v.findViewById<View>(R.id.cl).setOnClickListener {}
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(0x55000000))

        ivClose.setOnClickListener {
            dismiss()
        }
        val distance = AMapUtils.calculateLineDistance(LatLng(agent.lat, agent.lng), LatLng(App.lat, App.lng))
        val distanceStr =
            if (distance >= 1000)
                "${String.format("%.2f", (distance / 1000))}km"
            else
                "${String.format("%.2f", distance)}m"


        tvStationName.text = agent.site_name
        tvStationPhone.text = agent.tel
        tvStationDistance.text = "距离我${distanceStr}"
        tvStationLocation.text = agent.address
        tvStationTag.text = agent.tag
        v.findViewById<ImageView>(R.id.iv01).setImageResource(if (type == "3") R.mipmap.marker_net_station_big else R.mipmap.service_station_big)

        v.findViewById<TextView>(R.id.tv_phone_call).setOnClickListener {
            markCallback.click(it, agent)
        }
//        tvStationBatteryCount.visibility = if (type == "3") View.VISIBLE else View.GONE
        tvStationBatteryCount.text = "${agent.count}台"

        v.findViewById<TextView>(R.id.tv_in_shop).setOnClickListener {
            dismiss()
            markCallback.click(it, agent)
        }

        v.findViewById<TextView>(R.id.tv_navi_here).setOnClickListener {
            markCallback.click(it, agent)
        }




        show(activity.window.decorView)
    }

    public interface OnMarKCallback {
        fun click(view: View, agent: NetStationBean.Data.X)
    }

    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.BOTTOM, 0, 0)
        }
    }


}