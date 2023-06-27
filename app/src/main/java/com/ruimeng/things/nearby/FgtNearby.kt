package   com.ruimeng.things.nearby

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundFrameLayout
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHomeBack
import com.ruimeng.things.nearby.bean.LBSNearbyBean
import wongxd.base.MainTabFragment
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
 * Created by wongxd on 2018/11/9.
 */
class FgtNearby : MainTabFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_nearby

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        initTopbar(mView?.findViewById(R.id.topbar), "附近", false)

        getPermissions(activity,
            PermissionType.COARSE_LOCATION,
            PermissionType.FINE_LOCATION,
            PermissionType.ACCESS_WIFI_STATE,
            PermissionType.ACCESS_NETWORK_STATE,
            allGranted = {
                afterGetPermission(savedInstanceState)
            })
    }


    private fun afterGetPermission(savedInstanceState: Bundle?) {


        val flScan = rootView?.findViewById<QMUIRoundFrameLayout>(R.id.fl_scan_nearby)
        flScan?.setOnClickListener { FgtHomeBack.selectDeviceType() }

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

            override fun getInfoWindow(marker: Marker?): View {

                val v = View.inflate(activity, R.layout.layout_agent_nearby_infowindow, null)
                val tvAgentName = v.findViewById<TextView>(R.id.tv_agent_name)
                val tvAgentPhone = v.findViewById<TextView>(R.id.tv_agent_phone)
                val tvAgentLocation = v.findViewById<TextView>(R.id.tv_agent_location)
                val tvAgentMsg = v.findViewById<TextView>(R.id.tv_agent_msg)
                val btnNavi = v.findViewById<Button>(R.id.btn_navi_here)

                marker?.let {

                    val agent = markInfoMap[marker.position.latitude + marker.position.longitude]

                    agent?.let {
                        tvAgentName.text = agent.nickname
                        tvAgentPhone.text = agent.mobile
                        tvAgentLocation.text = agent.address
                        tvAgentMsg.text = agent.notice

                        tvAgentPhone.setOnClickListener {
                            SystemUtils.call(activity, agent.mobile)
                        }

                        btnNavi.setOnClickListener {
                            naviToLocation(marker, agent.nickname)
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

        val myLocationStyle: MyLocationStyle
        myLocationStyle =
            MyLocationStyle()//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
        myLocationStyle.interval(2000) //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。

        aMap?.setMyLocationStyle(myLocationStyle)//设置定位蓝点的Style
        aMap?.getUiSettings()?.setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap?.setMyLocationEnabled(true)// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        aMap?.setOnMyLocationChangeListener { location ->
            mCurrentLat = location.latitude
            mCurrentLon = location.longitude

            getNearby()
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
        val mapScreenMarkers: MutableList<Marker>? = aMap?.getMapScreenMarkers()
        mapScreenMarkers?.let {
            for (item in mapScreenMarkers) {
                item.remove()//移除当前Marker
            }

            aMap?.reloadMap()//刷新地图
        }
    }


    /**
     * 找到电池的位置
     */
    private fun getNearby() {

        http {

            url = Path.LBS_AGENT

            params["lat"] = mCurrentLat.toString()

            params["lng"] = mCurrentLon.toString()



            onSuccess {

                val result = it.toPOJO<LBSNearbyBean>().data

                result.forEach { agent ->
                    addNearbyAgentMarkder(agent)
                }
            }

            onFail { i, s ->
                EasyToast.DEFAULT.show(s)
            }
        }
    }


    private val markInfoMap: MutableMap<Double, LBSNearbyBean.Data> = mutableMapOf()

    private var markerOption: MarkerOptions? = null

    /**
     * 在地图上添加marker
     */
    private fun addNearbyAgentMarkder(agent: LBSNearbyBean.Data) {

        markerOption = MarkerOptions()
            .zIndex(10f)
            .position(LatLng(agent.lat, agent.lng))
            .draggable(false)


        val v = View.inflate(activity, R.layout.layout_nearby_marker, null)
        val iv = v.findViewById<ImageView>(R.id.iv)


        markerOption?.icon(BitmapDescriptorFactory.fromView(iv))

        aMap?.addMarker(markerOption)

        markInfoMap.put((agent.lat + agent.lng), agent)
    }

}