package com.ruimeng.things.home

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
// import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.Path
import com.ruimeng.things.R
import org.jetbrains.anko.textColor
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
import wongxd.utils.SystemUtils
import wongxd.utils.ToastUtils
import com.utils.CommonUtil


/**
 * Created by wongxd on 2018/11/12.
 */
class FgtFindBattery : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_find_batter

    // 移除右上角刷新按钮与动画

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "电池定位")
        mMapView = rootView?.findViewById(R.id.mapView) as MapView
        mMapView?.onCreate(savedInstanceState) // 此方法必须重写

        if (aMap == null) {
            aMap = mMapView?.map
            // 点击地图气球不再展示弹窗
            aMap?.setOnMarkerClickListener { true }
            // 关闭右下角缩放控件
            aMap?.uiSettings?.isZoomControlsEnabled = false
        }

        //设置希望展示的地图缩放级别
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(19f))

        // 绑定右下角刷新按钮点击事件，调用页面刷新
        rootView?.findViewById<View>(R.id.cv_refresh_battery)?.setOnClickListener {
            getBatteryLocation()
        }


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


        // 绑定导航按钮点击事件，调用第三方地图导航
        rootView?.findViewById<View>(R.id.cv_navi_battery)?.setOnClickListener {
            val lat = batteryLat
            val lng = batteryLng
            if (lat != null && lng != null) {
                val batteryId = FgtHome.CURRENT_DEVICEID
                val targetName = "电池$batteryId"
                val title = "导航前往“电池$batteryId”"
                CommonUtil.naviToLocation(requireActivity(), lat, lng, targetName, title)
            } else {
                EasyToast.DEFAULT.show("暂无定位，请先刷新")
            }
        }
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
    // 当前电池定位坐标（用于导航）
    private var batteryLat: Double? = null
    private var batteryLng: Double? = null


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

        // 移除右上角刷新按钮相关动画

        http {

            url = Path.DEVICE_GEO

            params["device_id"] = FgtHome.CURRENT_DEVICEID

            onFinish {
                // 无刷新按钮动画
            }

            onSuccess {

                //{"errcode":200,"errmsg":"\u64cd\u4f5c\u6210\u529f","data":{"device_id":1025,"geo":{"lat":30.537952,"lng":104.063519}}}
                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val geo = data.optJSONObject("geo")

                val lat = geo.optDouble("lat")
                val lng = geo.optDouble("lng")
                val timeline = geo.optInt("timeline")
                val address = ""
//                    geo.optString("province") + geo.getString("city") + geo.getString("area") + geo.getString(
//                        "address"
//                    )

//                val latLng = Converter.gps2gaode(gpsLat, gpsLng)

                // 更新右下角“更新时间”展示
                rootView?.findViewById<TextView>(R.id.tv_update_time)?.text = "更新时间：${formatTimelineSlash(timeline)}"
                // 保存电池坐标用于导航
                batteryLat = lat
                batteryLng = lng

                aMap?.clear()
                addBatteryMarkder(lat, lng, timeline, address)
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
    private fun addBatteryMarkder(lat: Double, lng: Double, timeline: Int,address: String) {
        markerOption = MarkerOptions()
            .zIndex(10f)
            .position(LatLng(lat, lng))
            .draggable(false)

        markerOption?.icon(BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
            requireContext().resources,
            R.mipmap.marker_battery
        )))

        aMap?.moveCamera(CameraUpdateFactory.changeLatLng(LatLng(lat, lng)))

        // 点击标记不展示气泡或弹窗
        aMap?.setOnMarkerClickListener { true }
        // 移除信息窗体适配，避免展示气泡

        aMap?.addMarker(markerOption)

//        val geocoder = GeocodeSearch(activity)
//        geocoder.getFromLocationAsyn(
//            RegeocodeQuery(
//                LatLonPoint(lat, lng),
//                100f,
//                GeocodeSearch.AMAP
//            )
//        )
//        geocoder.setOnGeocodeSearchListener(object : OnGeocodeSearchListener {
//            override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) {
//                Log.i("TAG", "onRegeocodeSearched: " + p1)
//                if (p1 == 1000 && p0 != null) {
//                    aMap?.clear()
//                    val v = View.inflate(activity, R.layout.layout_battery_marker, null)
//                    val tvTime = v.findViewById<TextView>(R.id.tv_time)
//                    val tvLocation = v.findViewById<TextView>(R.id.tv_location)
//                    tvLocation.text = p0.regeocodeAddress.formatAddress
//                    tvTime.text = timeline.toLong().getTime()
//                    markerOption?.icon(BitmapDescriptorFactory.fromView(v))
//                    aMap?.addMarker(markerOption)
//
//                    aMap?.moveCamera(CameraUpdateFactory.changeLatLng(LatLng(lat, lng)))
//
//
//                } else {
//                    ToastUtils.showShortSafe("状态码" + p1)
//                }
//            }
//
//            override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
//                Log.i("TAG", "onGeocodeSearched: ")
//            }
//
//        })

        
    }

    private fun formatTimelineSlash(timeline: Int): String {
        return try {
            val millis = timeline.toLong() * 1000L
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(millis))
        } catch (e: Exception) {
            "--/--/-- --:--:--"
        }
    }

}
