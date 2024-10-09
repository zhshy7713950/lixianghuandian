package com.ruimeng.things.net_station.view

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean
import com.utils.CommonUtil
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
        CommonUtil.naviToLocation(context as FragmentActivity,data.lat,data.lng,"站点位置",
            "导航前往${data.site_name}")
    }

    override fun bindView(view: INetStationView) {
    }
}