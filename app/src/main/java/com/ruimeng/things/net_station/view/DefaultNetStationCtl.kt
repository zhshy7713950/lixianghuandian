package com.ruimeng.things.net_station.view

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean
import wongxd.common.checkPackage
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.navi.CoodinateCovertor
import wongxd.navi.LngLat
import wongxd.navi.NaviUtil
import wongxd.utils.SystemUtils

class DefaultNetStationCtl : INetStationController {

    companion object {
        fun create(): INetStationController {
            return DefaultNetStationCtl()
        }
    }

    override fun onItemClick(data: NetStationBean.Data.X, context: Context) {
        // TODO 跳转详情
    }

    override fun onTelClick(data: NetStationBean.Data.X, context: Context) {
        val telData = if (data.telData.isNullOrEmpty()) {
            arrayListOf(NetStationBean.Data.TelData("00:00-23:59", "4000283969"))
        } else if (data.telData.size == 1) {
            data.telData
        } else {
            if (data.telData[0].phone == data.telData[1].phone) {
                arrayListOf(data.telData[0])
            } else {
                data.telData
            }
        }
        val bs = QMUIBottomSheet.BottomListSheetBuilder(context)
            .setTitle("联系电话")
        telData.forEach {
            bs.addItem("${it.duration} 时段：${it.phone}", it.phone)
        }
        bs.setOnSheetItemClickListener { dialog, _, _, tag ->
            val phone = tag as String
            getPermissions(context as FragmentActivity,
                PermissionType.CALL_PHONE,
                allGranted = { SystemUtils.call(context, phone) })
            dialog.dismiss()
        }
        bs.build().show()
    }

    override fun onNavigationClick(data: NetStationBean.Data.X, context: Context) {
        val appName = context.resources.getString(R.string.app_name)
        val latA = App.lat
        val lngA = App.lng
        val sName = "我的位置"

        val latB = data.lat
        val lngB = data.lng
        val dName = "站点位置"

        val bs = QMUIBottomSheet.BottomListSheetBuilder(context)
            .setTitle("导航前往${data.site_name}")
        if (checkPackage(context, "com.autonavi.minimap")) {
            bs.addItem("高德地图", "gd")
        }
        if (checkPackage(context, "com.baidu.BaiduMap")) {
            bs.addItem("百度地图", "bd")
        }

        if (!checkPackage(context, "com.autonavi.minimap")
            &&
            !checkPackage(context, "com.baidu.BaiduMap")
        ) {
            bs.addItem("请先下载“高德地图” 或 “百度地图”", "no")
        }
        bs.setOnSheetItemClickListener { dialog, itemView, position, tag ->
            if (tag == "gd") {
                NaviUtil.setUpGaodeAppByLoca(
                    appName,
                    latA.toString(), lngA.toString(), sName,
                    latB.toString(), lngB.toString(), dName
                )
            } else if (tag == "bd") {

                val posA = LngLat()
                posA.latitude = latA
                posA.longitude = lngA

                val posB = LngLat()
                posB.latitude = latB
                posB.longitude = lngB

                val bdA = CoodinateCovertor.bd_encrypt(posA)
                val bdB = CoodinateCovertor.bd_encrypt(posB)

                NaviUtil.setUpBaiduAPPByLoca(
                    bdA.latitude.toString(), bdA.longitude.toString(), sName,
                    bdB.latitude.toString(), bdB.longitude.toString(), dName,
                    appName, appName
                )
            }
            dialog.dismiss()

        }
        bs.build().show()
    }

    override fun bindView(view: INetStationView) {
    }
}