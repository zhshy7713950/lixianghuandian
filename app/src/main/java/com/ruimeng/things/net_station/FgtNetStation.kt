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
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.sp
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
class FgtNetStation : MainTabFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_net_station

    private var currentIndex = 0
    override fun initView(mView: View?, savedInstanceState: Bundle?) {

        mView?.findViewById<QMUITopBar>(R.id.topbar)?.apply {
            initTopbar(this, "网点", false)
            addRightImageButton(R.mipmap.map, R.id.right)
                .setOnClickListener {
                    LogHelper.i(
                        "data===",
                        "===currentIndex===${currentIndex}===titleList[currentIndex]===${titleList[currentIndex]}"
                    )
                    val type = when {
                        "售后服务网点" == titleList[currentIndex] -> {"1"}
                        "租电服务站点" == titleList[currentIndex] -> {"2"}
                        else -> {"3"}
                    }
                    FgtMain.instance?.start(FgtNetStationByMap.newInstance(type,"","",fragmentList.get(currentIndex).getStationList()))
                }
        }

        requestNetWorkShow()

    }

//    private var allFragmentList = ArrayList<SupportFragment>()
    //    private var fragmentList = arrayOf<SupportFragment>()
    //    private var fragmentList = arrayOf(FgtNetStationItem.newInstance("2"))
    private var titleList = ArrayList<String>()
    private var fragmentList = ArrayList<FgtNetStationItem>()

    private fun requestNetWorkShow() {
        http {
            url = "apiv4/networkshow"
            onSuccessWithMsg { res, _ ->
                val data = res.toPOJO<NetWorkShowBean>().data
                titleList.clear()
                if (data.cg_show != null){
                    fragmentList.add( FgtNetStationItem.newInstance("3"))
                    titleList.add("换电站点")
                }
                if (data.cg_rent_show == 0){
//                    fragmentList.add( FgtNetStationItem.newInstance("2"))
//                    titleList.add("换电站点")
                }
                if (data.cg_service_show != null){
                    fragmentList.add( FgtNetStationItem.newInstance("1"))
                    titleList.add("售后服务网点")
                }
                setView(fragmentList.toTypedArray())
//
//                if ((data.cg_service_show != 0) && (data.cg_rent_show != 0) && (data.cg_show != 0)) {//111
//                    val fragmentList = arrayOf<SupportFragment>(
//                        FgtNetStationItem.newInstance("3"),
//                        FgtNetStationItem.newInstance("2"),
//                        FgtNetStationItem.newInstance("1")
//                    )
//                    titleList.add("换电站点")
//                    titleList.add("租电服务站点")
//                    titleList.add("售后服务网点")
//                    setView(fragmentList)
//                } else if ((data.cg_service_show != 0) && (data.cg_rent_show == 0) && (data.cg_show == 0)) { //100
//                    val fragmentList = arrayOf<SupportFragment>(
//                        FgtNetStationItem.newInstance("1")
//                    )
//                    titleList.add("售后服务网点")
//                    setView(fragmentList)
//                } else if ((data.cg_service_show != 0) && (data.cg_rent_show == 0) && (data.cg_show != 0)
//                ) { //101
//                    val fragmentList = arrayOf<SupportFragment>(
//                        FgtNetStationItem.newInstance("3"),
//                        FgtNetStationItem.newInstance("1")
//                    )
//
//                    titleList.add("换电站点")
//                    titleList.add("售后服务网点")
//                    setView(fragmentList)
//                } else if ((data.cg_service_show != 0) && (data.cg_rent_show != 0) && (data.cg_show == 0)
//                ) { // 110
//                    val fragmentList = arrayOf<SupportFragment>(
//                        FgtNetStationItem.newInstance("2"),
//                        FgtNetStationItem.newInstance("1")
//                    )
//
//                    titleList.add("租电服务站点")
//                    titleList.add("售后服务网点")
//                    setView(fragmentList)
//                } else if ((data.cg_service_show == 0) && (data.cg_rent_show != 0) && (data.cg_show != 0)
//                ) { //  011
//                    val fragmentList = arrayOf<SupportFragment>(
//                        FgtNetStationItem.newInstance("3"),
//                        FgtNetStationItem.newInstance("2")
//                    )
//
//                    titleList.add("换电站点")
//                    titleList.add("租电服务站点")
//                    setView(fragmentList)
//                } else if ((data.cg_service_show == 0) && (data.cg_rent_show != 0) && (data.cg_show == 0)
//                ) {
//                    // 010
//                    val fragmentList = arrayOf<SupportFragment>(
//                        FgtNetStationItem.newInstance("2")
//                    )
//                    titleList.add("租电服务站点")
//                    setView(fragmentList)
//                } else if ((data.cg_service_show == 0) && (data.cg_rent_show == 0) && (data.cg_show != 0)
//                ) { //001
//                    val fragmentList = arrayOf<SupportFragment>(
//                        FgtNetStationItem.newInstance("3")
//                    )
//                    titleList.add("换电站点")
//                    setView(fragmentList)
//                }
//                else { //000
//
//                }

            }

            onFail { _, msg ->
                ToastHelper.shortToast(activity, msg)
            }
        }
    }

    private fun setView(list: Array<SupportFragment>) {
        tab_net_station.apply {
            reset()
            for (i in titleList.indices) {
                addTab(QMUITabSegment.Tab(titleList[i]))
            }
            setTabTextSize(sp(14))
            setHasIndicator(true)
            setIndicatorWidthAdjustContent(true)
            setIndicatorDrawable(resources.getDrawable(R.drawable.line_green))
            setDefaultNormalColor(Color.parseColor("#8694A0"))
            setDefaultSelectedColor(resources.getColor(R.color.white))
            setOnTabClickListener { index ->
                currentIndex = index
                showHideFragment(
                    fragmentList[index]
                )
            }
            selectTab(0)
            notifyDataChanged()
        }

        val dlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "定位中", true)
        dlg.show()
        getPermissions(activity,
            PermissionType.COARSE_LOCATION,
            PermissionType.FINE_LOCATION,
            PermissionType.ACCESS_NETWORK_STATE,
            PermissionType.ACCESS_WIFI_STATE,
            PermissionType.READ_PHONE_STATE,

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