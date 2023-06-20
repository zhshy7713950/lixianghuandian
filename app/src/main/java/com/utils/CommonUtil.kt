package com.utils

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.MapSelectPopup
import wongxd.common.checkPackage
import wongxd.navi.CoodinateCovertor
import wongxd.navi.LngLat
import wongxd.navi.NaviUtil

object CommonUtil {
    fun naviToLocation(activity: FragmentActivity, targetLat: Double, targetLng: Double, targetName: String) {

        val appName = activity.getString(R.string.app_name)
        val latA = App.lat
        val lngA = App.lng
        val sName = "我的位置"

        val latB = targetLat
        val lngB = targetLng
        val dName = targetName

        var listener = View.OnClickListener{
            when(it.id){
             R.id.tvAmap->{
                 NaviUtil.setUpGaodeAppByLoca(
                     appName,
                     latA.toString(), lngA.toString(), sName,
                     latB.toString(), lngB.toString(), dName
                 )
             }
             R.id.tvBaidu->{
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
            }
        }
        MapSelectPopup(activity,listener)

    }
}