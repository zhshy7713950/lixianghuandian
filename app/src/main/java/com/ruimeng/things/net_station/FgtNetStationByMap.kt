package com.ruimeng.things.net_station

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.checkPackage
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import wongxd.navi.CoodinateCovertor
import wongxd.navi.LngLat
import wongxd.navi.NaviUtil
import wongxd.utils.SystemUtils

/**
 * Created by wongxd on 2019/7/3.
 */
class FgtNetStationByMap : BaseBackFragment() {
    companion object {
        fun newInstance(type: String = ""): FgtNetStationByMap {
            val fgt = FgtNetStationByMap()
            val b = Bundle()
            b.putString("type", type)
            fgt.arguments = b
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_net_station_by_map

    private val getType by lazy { arguments?.getString("type", "") ?: "" }
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "地图查找")
        getPermissions(
            activity,
            PermissionType.COARSE_LOCATION,
            PermissionType.FINE_LOCATION,
            PermissionType.ACCESS_WIFI_STATE,
            PermissionType.ACCESS_NETWORK_STATE,
            allGranted = {
                afterGetPermission(savedInstanceState)
            }
        )
    }

    private fun afterGetPermission(savedInstanceState: Bundle?) {


        mMapView = rootView?.findViewById(R.id.mapView_nearby) as MapView?
        mMapView?.onCreate(savedInstanceState) // 此方法必须重写

        if (aMap == null) {
            aMap = mMapView?.map
            aMap?.setOnMarkerClickListener { marker ->

                marker.showInfoWindow()
                true
            }
        }

        //设置希望展示的地图缩放级别
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(19f))


        //        aMap?.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
        //            override fun onCameraChange(p0: CameraPosition?) {
        //
        //            }
        //
        //            override fun onCameraChangeFinish(p0: CameraPosition?) {
        //                p0?.let {
        //                    mCurrentLat = it.target.latitude
        //                    mCurrentLon = it.target.longitude
        //                    getRedPackets()
        //                }
        //            }
        //        })


        aMap?.setInfoWindowAdapter(object : AMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker?): View? {
                return null
            }

            @SuppressLint("SetTextI18n")
            override fun getInfoWindow(marker: Marker?): View {

                val v = View.inflate(activity, R.layout.layout_station_infowindow, null)
                val ivClose = v.findViewById<ImageView>(R.id.iv_close)
                val tvStationName = v.findViewById<TextView>(R.id.tv_station_name)
                val tvStationPhone = v.findViewById<TextView>(R.id.tv_station_phone)
                val tvStationDistance = v.findViewById<TextView>(R.id.tv_station_distance)
                val tvStationLocation = v.findViewById<TextView>(R.id.tv_station_location)
                val tvStationTag = v.findViewById<TextView>(R.id.tv_station_tag)
                val tvStationBatteryCount = v.findViewById<TextView>(R.id.tv_station_battery_count)

                val tvIntoShop = v.findViewById<TextView>(R.id.tv_into_shop)
                val tvNavi = v.findViewById<TextView>(R.id.tv_navi_here)


                marker?.let { markerView ->

                    ivClose.setOnClickListener {
                        markerView.hideInfoWindow()
                    }


                    val agent = markInfoMap[marker.position.latitude + marker.position.longitude]

                    agent?.let {

                        val distance =
                            AMapUtils.calculateLineDistance(
                                LatLng(agent.lat, agent.lng),
                                LatLng(App.lat, App.lng)
                            )
                        val distanceStr =
                            if (distance >= 1000)
                                "${String.format("%.2f", (distance / 1000))}km"
                            else
                                "${String.format("%.2f", distance)}m"


                        tvStationName.text = agent.site_name
                        tvStationPhone.text = agent.tel
                        tvStationDistance.text = distanceStr
                        tvStationLocation.text = agent.address
                        tvStationTag.text = agent.tag

                        tvStationPhone.setOnClickListener {
                            getPermissions(activity,
                                PermissionType.CALL_PHONE,
                                allGranted = { SystemUtils.call(activity, agent.tel) })
                        }

                        tvStationBatteryCount.text = "可换电池数：${agent.count}台"

                        tvIntoShop.setOnClickListener {
                            if ("3"==getType){
                                start(FgtNetStationDetailTwo.newInstance(agent.site_name, agent.id))
                            }else{
                                start(FgtNetStationDetail.newInstance(agent.site_name, agent.id))
                            }

                        }

                        tvNavi.setOnClickListener {
                            naviToLocation(marker, agent.site_name)
                        }
                    }


                }


                return v
            }
        })


        showPosInMap()
    }

    private fun naviToLocation(marker: Marker, title: String) {

        val appName = getString(R.string.app_name)
        val latA = mCurrentLat
        val lngA = mCurrentLon
        val sName = "我的位置"

        val latB = marker.position.latitude
        val lngB = marker.position.longitude
        val dName = title


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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState)
    }


    private var aMap: AMap? = null
    private var mMapView: MapView? = null


    private var mCurrentLat = 0.0
    private var mCurrentLon = 0.0


    /**
     * 在地图上展示我的位置
     */
    private fun showPosInMap() {

        val myLocationStyle: MyLocationStyle = MyLocationStyle()
        //初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
        myLocationStyle.interval(2000) //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。

        aMap?.myLocationStyle = myLocationStyle//设置定位蓝点的Style
        aMap?.uiSettings?.isMyLocationButtonEnabled = true//设置默认定位按钮是否显示，非必需设置。
        aMap?.isMyLocationEnabled = true// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        aMap?.setOnMyLocationChangeListener { location ->
            mCurrentLat = location.latitude
            mCurrentLon = location.longitude

            getList()
        }
    }


    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onPause() {
        mMapView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        mMapView?.onDestroy()
        super.onDestroyView()
    }


    //删除指定Marker
    private fun clearMarkers() {
        //获取地图上所有Marker
        val mapScreenMarkers: MutableList<Marker>? = aMap?.mapScreenMarkers
        mapScreenMarkers?.let {
            for (item in mapScreenMarkers) {
                item.remove()//移除当前Marker
            }

            aMap?.reloadMap()//刷新地图
        }
    }


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
            params["city_id"] = ""

            onSuccess { res ->
                rootView?.let {
                    val data = res.toPOJO<NetStationBean>().data

                    val locations: MutableList<NetStationBean.Data.X> = mutableListOf()

                    data.forEach { item ->
                        locations.addAll(item.list)
                    }

                    locations.forEach { loc ->
                        addMarker(loc)
                    }

                    setSuitZoom(locations)
                }
            }

            onFail { i, s ->
                EasyToast.DEFAULT.show(s)
            }
        }
    }


    /**
     * 设置合适的缩放级别
     *
     * 20-10m-（19=<zoom<20）
    19-10m-（19=<zoom<20）
    18-25m-（18=<zoom<19）
    17-50m-（17=<zoom<18）
    16-100m-（16=<zoom<17）
    15-200m-（15=<zoom<16）
    14-500m-（14=<zoom<15）
    13-1km-（13=<zoom<14）
    12-2km-（12=<zoom<13）
    11-5km-（11=<zoom<12）
    10-10km-（10=<zoom<11）
    9-20km-（9=<zoom<10）
    8-30km-（8=<zoom<9）
    7-50km-（7=<zoom<8）
    6-100km-（6=<zoom<7）
    5-200km-（5=<zoom<6）
    4-500km-（4=<zoom<5）
    3-1000km-（3=<zoom<4）
    2-1000km-（3=<zoom<4）1
     */
    private fun setSuitZoom(list: List<NetStationBean.Data.X>) {

        var nearestDistance = 0f
        list.forEach {
            val distance = AMapUtils.calculateLineDistance(
                LatLng(mCurrentLat, mCurrentLon),
                LatLng(it.lat, it.lng)
            )

            if (nearestDistance > distance || nearestDistance == 0f) {
                nearestDistance = distance
            }

        }

        val zoomSize =
            when (nearestDistance) {
                in 10..100 -> 18f
                in 100..500 -> 15f
                in 500..1000 -> 14f
                in 1000..2000 -> 13f
                in 2000..5000 -> 12f
                in 1000..10000 -> 11f
                in 1000..20000 -> 10f
                in 1000..30000 -> 9f
                in 1000..50000 -> 8f
                in 1000..100000 -> 7f
                in 100000..1000000 -> 5f
                else -> 17f
            }

        mMapView?.map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    mCurrentLat,
                    mCurrentLon
                ), zoomSize
            )
        )
    }


    private val markInfoMap: MutableMap<Double, NetStationBean.Data.X> = mutableMapOf()

    private var markerOption: MarkerOptions? = null

    /**
     * 在地图上添加marker
     */
    private fun addMarker(agent: NetStationBean.Data.X) {

        markerOption = MarkerOptions()
            .zIndex(10f)
            .position(LatLng(agent.lat, agent.lng))
            .draggable(false)


        val v = View.inflate(activity, R.layout.layout_net_station_marker, null)
        val iv = v.findViewById<ImageView>(R.id.iv)


        markerOption?.icon(BitmapDescriptorFactory.fromView(iv))

        aMap?.addMarker(markerOption)

        markInfoMap.put((agent.lat + agent.lng), agent)
    }


}