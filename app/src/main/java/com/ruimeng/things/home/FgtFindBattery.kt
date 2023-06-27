package com.ruimeng.things.home

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.Path
import com.ruimeng.things.R
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.checkPackage
import wongxd.common.getTime
import wongxd.http
import wongxd.navi.Converter
import wongxd.navi.CoodinateCovertor
import wongxd.navi.LngLat
import wongxd.navi.NaviUtil


/**
 * Created by wongxd on 2018/11/12.
 */
class FgtFindBattery : BaseBackFragment() {
    override fun getLayoutRes(): Int = com.ruimeng.things.R.layout.fgt_find_batter

    private var right: QMUIAlphaImageButton? = null

    private val mAnimation by lazy { AnimationUtils.loadAnimation(activity, R.anim.rotate_repeat) }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "寻车")
        right = topbar.addRightImageButton(com.ruimeng.things.R.drawable.icon_refresh, com.ruimeng.things.R.id.right)
            .apply {
                setOnClickListener { view ->
                    clearMarkers()
                    getBatteryLocation()
                }

            }
        mMapView = rootView?.findViewById(com.ruimeng.things.R.id.mapView) as MapView
        mMapView?.onCreate(savedInstanceState) // 此方法必须重写

        if (aMap == null) {
            aMap = mMapView?.map
            aMap?.setOnMarkerClickListener { marker ->


                val appName = getString(com.ruimeng.things.R.string.app_name)
                val latA = mCurrentLat
                val lngA = mCurrentLon
                val sName = "我的位置"

                val latB = marker.position.latitude
                val lngB = marker.position.longitude
                val dName = "车辆位置"


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


        showPosInMap()
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

            getBatteryLocation()
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
    private fun getBatteryLocation() {

        right?.startAnimation(mAnimation)

        http {

            url = Path.DEVICE_GEO

            params["device_id"] = FgtHomeBack.CURRENT_DEVICEID

            onFinish {
                right?.clearAnimation()
            }

            onSuccess {

                //{"errcode":200,"errmsg":"\u64cd\u4f5c\u6210\u529f","data":{"device_id":1025,"geo":{"lat":30.537952,"lng":104.063519}}}
                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val geo = data.optJSONObject("geo")

                val gpsLat = geo.optDouble("lat")
                val gpsLng = geo.optDouble("lng")
                val timeline = geo.optInt("timeline")

                val latLng = Converter.gps2gaode(gpsLat, gpsLng)

                addBatteryMarkder(latLng.latitude, latLng.longitude, timeline)
            }

            onFail { i, s ->
                EasyToast.DEFAULT.show(s)
                pop()
            }
        }
    }

    private var markerOption: MarkerOptions? = null

    /**
     * 在地图上添加marker
     */
    private fun addBatteryMarkder(lat: Double, lng: Double, timeline: Int) {

        markerOption = MarkerOptions()
            .zIndex(10f)
            .position(LatLng(lat, lng))
            .draggable(false)


        val v = View.inflate(activity, com.ruimeng.things.R.layout.layout_battery_marker, null)
        val tvTime = v.findViewById<TextView>(com.ruimeng.things.R.id.tv_time)
        val iv = v.findViewById<ImageView>(com.ruimeng.things.R.id.iv)

        tvTime.text = timeline.toLong().getTime()

        markerOption?.icon(BitmapDescriptorFactory.fromView(v))

        aMap?.addMarker(markerOption)

        aMap?.moveCamera(CameraUpdateFactory.changeLatLng(LatLng(lat, lng)))

    }


}
