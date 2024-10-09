package com.ruimeng.things.net_station

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.animation.ScaleAnimation
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.net_station.bean.NetStationBean
import com.ruimeng.things.net_station.bean.getAvaModelNum
import com.ruimeng.things.net_station.view.DefaultNetStationCtl
import com.utils.GlideHelper
import com.utils.unsafeLazy
import kotlinx.android.synthetic.main.fgt_net_station_map.cv_location
import kotlinx.android.synthetic.main.fgt_net_station_map.cv_refresh
import kotlinx.android.synthetic.main.fgt_net_station_map.et_search
import kotlinx.android.synthetic.main.fgt_net_station_map.iv_close_net_station_view
import kotlinx.android.synthetic.main.fgt_net_station_map.net_station_view
import kotlinx.android.synthetic.main.fgt_net_station_map.tv_right
import kotlinx.android.synthetic.main.fgt_net_station_map.tv_search
import wongxd.base.MainTabFragment
import wongxd.common.EasyToast
import wongxd.common.getCurrentAty
import wongxd.common.getSweetDialog
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http

class FgtNetStationMap : MainTabFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_net_station_map

    private var aMap: AMap? = null
    private var mMapView: MapView? = null
    private var location: Location? = null
    private val markInfoMap: MutableMap<String, NetStationBean.Data.X> = mutableMapOf()
    private val markerMap: MutableMap<String, Marker> = mutableMapOf()
    private var mCurrentMemMarker: Marker? = null
    private var dlgProgress: SweetAlertDialog? = null
    private val netStationCtl by unsafeLazy {
        DefaultNetStationCtl.create()
    }

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        tv_right.setOnClickListener {

        }
        tv_search?.setOnClickListener {
            hideNetStationView()
            getNetStationList(et_search.text.toString())
        }
        cv_location.setOnClickListener {
            hideNetStationView()
            moveLocation()
        }
        cv_refresh.setOnClickListener {
            hideNetStationView()
            et_search.text.clear()
            getNetStationList()
        }
        iv_close_net_station_view.setOnClickListener {
            hideNetStationView()
        }
        getPermissions(
            activity,
            PermissionType.COARSE_LOCATION,
            PermissionType.FINE_LOCATION,
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
        }
        //设置希望展示的地图缩放级别
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(13f))
        initLocationData()
        showPosInMap()
        getNetStationList()
    }

    private fun initLocationData() {
        AMapLocUtils().getLonLat(activity?.applicationContext) {
            App.lat = it.latitude
            App.lng = it.longitude
            App.province = it.province
            App.city = it.city
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState)
    }

    private fun hideNetStationView(){
        net_station_view?.visibility = View.GONE
        iv_close_net_station_view?.visibility = View.GONE
    }

    private fun showNetStationView(data:NetStationBean.Data.X){
        net_station_view?.bindCtl(netStationCtl)
        net_station_view?.setNewData(data)
        net_station_view?.visibility = View.VISIBLE
        iv_close_net_station_view?.visibility = View.VISIBLE
    }

    /**
     * 在地图上展示我的位置
     */
    private fun showPosInMap() {
        val myLocationStyle = MyLocationStyle()
        //初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
        myLocationStyle.showMyLocation(true)

        aMap?.apply {
            this.myLocationStyle = myLocationStyle//设置定位蓝点的Style
            this.uiSettings?.isMyLocationButtonEnabled = false//设置默认定位按钮是否显示，非必需设置。
            this.uiSettings?.isZoomControlsEnabled = false
            this.isMyLocationEnabled = true// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
            this.setOnMyLocationChangeListener { location ->
                this@FgtNetStationMap.location = location
                moveLocation()
            }
            this.setOnMarkerClickListener {
                selectMarker(it)
                false
            }
        }
    }

    private var locations: MutableList<NetStationBean.Data.X> = mutableListOf()
    private fun getNetStationList(name: String = "") {
        dlgProgress = getSweetDialog(getCurrentAty(), SweetAlertDialog.PROGRESS_TYPE, "请求中...")
        dlgProgress!!.show()
        http {
            url = "apiv3/cgstationnetwork"
            params["city_id"] = "000000"
            params["name"] = name

            onSuccess { res ->
                rootView?.let {
                    val data = res.toPOJO<NetStationBean>().data
                    aMap?.clear()
                    locations.clear()
                    markerMap.clear()
                    markInfoMap.clear()

                    data.forEach { item ->
                        locations.addAll(item.list)
                    }
                    showMarkList()
                }
            }
            onFail { _, s ->
                EasyToast.DEFAULT.show(s)
            }
            onFinish {
                dlgProgress?.dismissWithAnimation()
            }
        }
    }

    private fun showMarkList() {
        locations.forEach { loc ->
            addMarker(loc)
        }
        showPosInMap()
    }

    private fun selectMarker(marker: Marker){
        if (marker != null) {
            mCurrentMemMarker?.startAnimation()
            setNotClickedMarkerAnim()
            mCurrentMemMarker = marker
            marker?.startAnimation()
            setClickedMarkerAnim()
            var agent = markInfoMap[marker.id]
            aMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(agent!!.lat,agent.lng)))
            aMap?.moveCamera(CameraUpdateFactory.zoomTo(15f))
            if (agent != null){
                showNetStationView(agent)
            }
        }
    }

    private fun setNotClickedMarkerAnim(){
        if (mCurrentMemMarker != null){
            var animation = ScaleAnimation(1.0f,1.6f,1.0f,1.6f)
            animation.setDuration(0)
            animation.fillMode = 1
            mCurrentMemMarker?.setAnimation(animation)
        }
    }

    private fun setClickedMarkerAnim(){
        if (mCurrentMemMarker != null){
            var animation = ScaleAnimation(1.6f,1.0f,1.6f,1.0f)
            animation.setDuration(0)
            animation.fillMode = 1
            mCurrentMemMarker?.setAnimation(animation)
        }
    }

    private fun addMarker(agent: NetStationBean.Data.X) {
        var imageUrl = "https://downxll.oss-cn-beijing.aliyuncs.com/lxhd/%s"
        imageUrl = if (agent.isOnline == 1) {
            val ava = agent.available_arr.getAvaModelNum(FgtHome.getBatteryV())
            String.format(
                imageUrl,
                String.format(
                    "mapballoon-change-%s-%s-%s@3x.png",
                    "small",
                    if (ava > 0) "green" else "yellow",
                    ava
                )
            )
        } else {
            String.format(imageUrl, "mapballoon-change-small-offline@3x.png")
        }
        context?.let {
            GlideHelper.loadImageAsBitmap(it, imageUrl) { bitmap ->
                if (bitmap != null) {
                    addMarkerInfo(agent, bitmap)
                }
            }
        }

    }

    private fun addMarkerInfo(agent: NetStationBean.Data.X,markerBitmap: Bitmap){
        var markerOption = MarkerOptions()
            .zIndex(10f)
            .position(LatLng(agent.lat, agent.lng))
            .draggable(false)
        markerOption?.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
        var marker = aMap?.addMarker(markerOption)
        if (marker != null){
            var animation = ScaleAnimation(1.0f,1.6f,1.0f,1.6f)
            animation.setDuration(0)
            animation.fillMode = 1
            marker.setAnimation(animation)
            marker.isClickable = true
            agent.markerId = marker.id
            markInfoMap[marker.id] = agent
            markerMap[marker.id] = marker
        }
    }

    private fun moveLocation() {
        this.location?.let {
            aMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it?.latitude, it?.longitude)))
            aMap?.moveCamera(CameraUpdateFactory.zoomTo(13f))
        }
    }
}