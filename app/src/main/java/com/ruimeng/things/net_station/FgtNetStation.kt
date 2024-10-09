package com.ruimeng.things.net_station

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.qmuiteam.qmui.widget.QMUITopBar
import com.ruimeng.things.App
import com.ruimeng.things.FgtMain
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetWorkShowBean
import com.ruimeng.things.net_station.net_city_data.CityDataWorker
import com.utils.LogHelper
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_balance_withdrawal.*
import kotlinx.android.synthetic.main.fgt_net_station.*
import kotlinx.android.synthetic.main.fgt_net_station_item.srl_station
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.sp
import wongxd.base.BaseBackFragment
import wongxd.base.MainTabFragment
import wongxd.common.getSweetDialog
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.utilcode.util.SPUtils

/**
 * Created by wongxd on 2019/7/3.
 */
class FgtNetStation : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_net_station

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "网点列表", true)
        topbar.addRightTextButton("刷新", R.id.right).apply {
            setTextColor(Color.WHITE)
            setOnClickListener {
                fragmentList[0].refresh()
            }
        }
        requestNetWorkShow()
    }

    private var fragmentList = ArrayList<FgtNetStationItem>()
    private fun requestNetWorkShow() {
        fragmentList.add(FgtNetStationItem.newInstance())
        setView(fragmentList.toTypedArray())
    }

    private fun setView(list: Array<SupportFragment>) {
        showHideFragment(
            fragmentList[0]
        )
        val dlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "定位中", true)
        dlg.show()
        getPermissions(activity,
            PermissionType.COARSE_LOCATION,
            PermissionType.FINE_LOCATION,

            allGranted = {

                AMapLocUtils().getLonLat(activity?.applicationContext) {
                    Log.i("TAG", "setView: ${it.province}:${it.city}")
                    App.lat = it.latitude
                    App.lng = it.longitude
                    App.province = it.province
                    App.city = it.city
                    SPUtils.getInstance().put("SP_PROVINCE",it.province)
                    SPUtils.getInstance().put("SP_City",it.city)
                    loadMultipleRootFragment(R.id.fl_net_station, 0, *list)
                    dlg.dismissWithAnimation()
                }

            })

        CityDataWorker.initJsonData()
    }


}