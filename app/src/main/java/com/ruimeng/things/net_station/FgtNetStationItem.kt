package com.ruimeng.things.net_station

import android.media.Image
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.qmuiteam.qmui.widget.QMUIFloatLayout
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean
import com.ruimeng.things.net_station.net_city_data.CityDataWorker
import com.ruimeng.things.net_station.net_city_data.NetCityJsonBean
import com.utils.DensityHelper
import kotlinx.android.synthetic.main.fgt_net_station_item.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import wongxd.base.MainTabFragment
import wongxd.common.bothNotNull
import wongxd.common.checkPackage
import wongxd.common.getSweetDialog
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import wongxd.navi.CoodinateCovertor
import wongxd.navi.LngLat
import wongxd.navi.NaviUtil
import wongxd.utils.SystemUtils



class FgtNetStationItem : MainTabFragment() {

    companion object {
        fun newInstance(type: String = ""): FgtNetStationItem {
            val fgt = FgtNetStationItem()
            val b = Bundle()
            b.putString("type", type)
            fgt.arguments = b
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_net_station_item

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        srl_station?.setEnableLoadMore(false)
        srl_station.setOnRefreshListener { getList() }

//        rv_city.layoutManager = LinearLayoutManager(activity)
//        rv_city.adapter = cityAdapter

        rv_station.layoutManager = LinearLayoutManager(activity)
        rv_station.adapter = stationAdapter

        CityDataWorker.initJsonData()


//        tv_province.setOnClickListener {
//            CityDataWorker.showOptionPicker(activity, "") { p, c ->
//                provice = p
//                city = c
//                refreshCityPickerState()
//            }
//        }
        tv_city.setOnClickListener {
                        CityDataWorker.showOptionPicker(activity, "") { p, c ->
                provice = p
                city = c
                refreshCityPickerState()
            }
        }



        dealSelectCurrentCity()

    }

    /**
     *  网点 服务站点和退还站点 默认定位选择到当前市
     */
    private fun dealSelectCurrentCity() {

        fun afterGetCityId(
            p: NetCityJsonBean.Data?,
            c: NetCityJsonBean.Data.Child?,
            dlg: SweetAlertDialog?
        ) {
            provice = p
            city = c
            refreshCityPickerState()
            getList()
            dlg?.dismissWithAnimation()
        }

        val (p, c) = CityDataWorker.getProvinceAndCityInfoByName(App.province, App.city)

        val dlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "获取城市数据中", true)
        dlg.show()
        if (null == p) {
            val (pp, cc) = CityDataWorker.getProvinceAndCityInfoByName(App.province, App.city)
            doAsync {
                Thread.sleep(2000)
                uiThread {
                    tv_city?.let {
                        afterGetCityId(pp, cc, dlg)
                    }
                }
            }

        } else {
            afterGetCityId(p, c, dlg)
        }
    }


    private var provice: NetCityJsonBean.Data? = null
    private var city: NetCityJsonBean.Data.Child? = null
    private var oldCity: NetCityJsonBean.Data.Child? = null

    private fun refreshCityPickerState() {
//        tv_city_picker_pre.text = if (null == city) "请选择" else "已选择"
//        tv_province.text = provice?.name?.replace("省", "") ?: ""
        tv_city.text = city?.name
        if (oldCity != city) {
            oldCity = city
            getList()
        }
    }


    private val getType by lazy { arguments?.getString("type", "") ?: "" }
    private var currentIndex = -1
    private val cityAdapter by lazy { CityRvAdapter { stationAdapter.setNewData(data[currentIndex].list) } }
    private val stationAdapter by lazy { StationRvAdapter() }

    private var data: List<NetStationBean.Data> = emptyList()

    private fun getList() {

        http {
            url = when (getType) {
                "1" -> {
                    "apiv3/servicenetwork"
                }
                "2" -> {
                    "apiv3/returnnetwork"
                }
                else -> {
                    "apiv3/cgstationnetwork"
                }
            }
            params["city_id"] = city?.id ?: ""

            onFinish { srl_station?.finishRefresh() }

            onSuccess { res ->
                srl_station?.let {
                    data = res.toPOJO<NetStationBean>().data
                    if (data.isEmpty()){
                        tv_empty_net_station.visibility =View.VISIBLE
                        stationAdapter.setNewData(null)
                    }else{
                        tv_empty_net_station.visibility =View.GONE
                        val citys = data.map { item -> item.city }
                        currentIndex = 0
                        stationAdapter.setNewData(data[currentIndex].list)
                    }

                }
            }
        }
    }

    private fun naviToLocation(targetLat: Double, targetLng: Double, targetName: String) {

        val appName = getString(R.string.app_name)
        val latA = App.lat
        val lngA = App.lng
        val sName = "我的位置"

        val latB = targetLat
        val lngB = targetLng
        val dName = targetName


        val bs = QMUIBottomSheet.BottomListSheetBuilder(activity)
            .setTitle("选择应用进行导航")



        if (checkPackage(activity!!, "com.autonavi.minimap")) {

            bs.addItem("高德地图", "gd")

        }
        if (checkPackage(activity!!, "com.baidu.BaiduMap")) {

            bs.addItem("百度地图", "bd")

        }

        if (!checkPackage(activity!!, "com.autonavi.minimap")
            &&
            !checkPackage(activity!!, "com.baidu.BaiduMap")
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

    inner class CityRvAdapter(val click: () -> Unit) :
        BaseQuickAdapter<String, BaseViewHolder>(com.ruimeng.things.R.layout.item_rv_city_station) {
        override fun convert(helper: BaseViewHolder, item: String?) {
            bothNotNull(helper, item) { a, b ->
                if (currentIndex == -1) {
                    currentIndex = 0
                    click.invoke()
                }
                val isChecked = a.layoutPosition == currentIndex
                val ll = a.getView<LinearLayout>(com.ruimeng.things.R.id.ll)
                val v = a.getView<View>(com.ruimeng.things.R.id.v)
                val tv = a.getView<TextView>(com.ruimeng.things.R.id.tv)


                if (isChecked) {
                    tv.setTextColor(resources.getColor(com.ruimeng.things.R.color.app_color))
                    ll.setBackgroundColor(resources.getColor(com.ruimeng.things.R.color.bg_gray))
                    v.setBackgroundColor(resources.getColor(com.ruimeng.things.R.color.app_color))
                } else {
                    tv.setTextColor(resources.getColor(com.ruimeng.things.R.color.text_color))
                    ll.setBackgroundColor(resources.getColor(com.ruimeng.things.R.color.white))
                    v.setBackgroundColor(resources.getColor(com.ruimeng.things.R.color.white))
                }

                tv.text = b

                a.itemView.setOnClickListener {
                    currentIndex = a.layoutPosition
                    click.invoke()
                    notifyDataSetChanged()
                }

            }
        }
    }


    inner class StationRvAdapter :
        BaseQuickAdapter<NetStationBean.Data.X, BaseViewHolder>(R.layout.item_rv_station) {
        override fun convert(helper: BaseViewHolder, item: NetStationBean.Data.X?) {
            bothNotNull(helper, item) { a, b ->
                val tvCall = a.getView<TextView>(R.id.tv_call)
                val tvTitle = a.getView<TextView>(R.id.tv_title)
                val tvLocation = a.getView<TextView>(R.id.tv_location)
                val tvDistance = a.getView<TextView>(R.id.tv_distance)


                a.itemView.setOnClickListener {
                    if ("3"==getType){
                        (parentFragment as FgtNetStation).start(
                            FgtNetStationDetailTwo.newInstance(
                                b.site_name,
                                b.id
                            )
                        )
                    }else{
                        (parentFragment as FgtNetStation).start(
                            FgtNetStationDetail.newInstance(
                                b.site_name,
                                b.id
                            )
                        )
                    }

                }

                tvCall.setOnClickListener {
                    getPermissions(activity, PermissionType.CALL_PHONE, allGranted = {
                        SystemUtils.call(context, b.tel)
                    })
                }

                tvTitle.text = b.site_name

                val distance =
                    AMapUtils.calculateLineDistance(LatLng(b.lat, b.lng), LatLng(App.lat, App.lng))
                val distanceStr =
                    if (distance >= 1000)
                        "${String.format("%.2f", (distance / 1000))}km"
                    else
                        "${String.format("%.2f", distance)}m"
                tvDistance.text = "距离我${distanceStr}"

                tvLocation.text = "地址：${b.address}"

                a.getView<TextView>(R.id.tv_nav).setOnClickListener {
                    naviToLocation(b.lat, b.lng, b.site_name)
                }
                val isReturnStation = "1" != getType

                val tvCode = a.getView<TextView>(R.id.tv_code)
                val tvNumber = a.getView<TextView>(R.id.tv_number)
                val imageView = a.getView<ImageView>(R.id.iv01)
                if (!isReturnStation) {
                    tvCode.text =  "代理编码：${b.tag}"
                    tvNumber.text = "可租用电池：${b.count}"
                    tvCode.visibility = View.VISIBLE
                    tvNumber.visibility = View.VISIBLE
                    imageView.setImageResource(R.mipmap.ic_shouhou)
                }else{
                    tvCode.visibility = View.GONE
                    tvNumber.visibility = View.GONE
                    imageView.setImageResource(R.mipmap.ic_statation)
                }
            }
        }

    }


}