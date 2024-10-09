package com.ruimeng.things.net_station.view

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.net_station.bean.NetStationBean
import com.ruimeng.things.net_station.bean.NetStationDetailBeanTwo
import com.utils.CommonUtil
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.utils.SystemUtils

abstract class AbsNetStationDetailCtl : INetStationDetailController {

    override fun onTelClick(data: NetStationDetailBeanTwo.Data, context: Context) {
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

    override fun onNavigationClick(data: NetStationDetailBeanTwo.Data, context: Context) {
        CommonUtil.naviToLocation(context as FragmentActivity,data.lat,data.lng,"站点位置",
            "导航前往${data.site_name}")
    }

    override fun bindView(view: INetStationDetailView) {
    }
}