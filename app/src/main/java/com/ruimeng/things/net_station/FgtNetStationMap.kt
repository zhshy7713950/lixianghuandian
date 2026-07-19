package com.ruimeng.things.net_station

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
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
import com.ruimeng.things.FgtMain
import com.ruimeng.things.MainViewModel
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.net_station.bean.NetStationGroupParser
import com.ruimeng.things.net_station.bean.StationGroup
import com.ruimeng.things.net_station.bean.filterGroupCabinets
import com.ruimeng.things.net_station.view.DefaultNetStationCtl
import com.utils.DensityUtil
import com.utils.GlideHelper
import com.utils.unsafeLazy
import kotlinx.android.synthetic.main.fgt_net_station_map.*
import wongxd.base.MainTabFragment
import wongxd.common.EasyToast
import wongxd.common.getCurrentAty
import wongxd.common.getSweetDialog
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.permission.isAllGrantedPermissions
import wongxd.http

class FgtNetStationMap : MainTabFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_net_station_map

    private val mainViewModel: MainViewModel by activityViewModels()
    private var aMap: AMap? = null
    private var mMapView: MapView? = null
    private var location: Location? = null
    private val markGroupMap: MutableMap<String, StationGroup> = mutableMapOf()
    private val markerMap: MutableMap<String, Marker> = mutableMapOf()
    private var mCurrentMemMarker: Marker? = null
    private var currentGroup: StationGroup? = null
    private var dlgProgress: SweetAlertDialog? = null
    private val netStationCtl by unsafeLazy {
        DefaultNetStationCtl.create()
    }
    private var savedInstanceState: Bundle? = null
    private var lastRefreshDeviceId: String? = null

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        tv_right.setOnClickListener {
            FgtMain.instance?.start(FgtNetStation())
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
            granterResult = {
                showHidePermission()
                afterGetPermission(savedInstanceState)
            },
            allGranted = {
                afterGetPermission(savedInstanceState)
            }
        )
    }

    private fun showHidePermission() {
        val isAllGranted = isAllGrantedPermissions(
            activity, PermissionType.COARSE_LOCATION,
            PermissionType.FINE_LOCATION
        )
        iv_permission?.visibility = if (isAllGranted) View.GONE else View.VISIBLE
        if (isAllGranted) {
            afterGetPermission(savedInstanceState)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAdded && isVisible) {
            showHidePermission()
        }
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        refreshForDeviceChangeIfNeeded()
    }

    private fun refreshForDeviceChangeIfNeeded() {
        val currentDeviceId = FgtHome.CURRENT_DEVICEID
        if (lastRefreshDeviceId != null && lastRefreshDeviceId != currentDeviceId) {
            hideNetStationView()
            et_search.text.clear()
            getNetStationList()
        }
    }

    private fun afterGetPermission(savedInstanceState: Bundle?) {
        mMapView = rootView?.findViewById(R.id.mapView_nearby) as MapView?
        mMapView?.onCreate(savedInstanceState) // 此方法必须重写
        if (aMap == null) {
            aMap = mMapView?.map
        }
        //设置希望展示的地图缩放级别
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(13f))
        aMap?.setOnMarkerClickListener {
            selectMarker(it)
            false
        }
        initLocationData()
        showPosInMap()
        getNetStationList()
    }

    private fun initLocationData() {
        mainViewModel.requestCityInfo(requireContext())
//        LocationUtil.getLocation(requireContext(),object : LocationUtil.Companion.LocationCallback {
//            override fun onLocationReceived(location: Location) {
//                App.lat = location.latitude
//                App.lng = location.longitude
//            }
//
//            override fun onLocationFailed(errorMessage: String) {
//            }
//        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState)
    }

    private fun hideNetStationView() {
        net_station_view?.visibility = View.GONE
        iv_close_net_station_view?.visibility = View.GONE
        station_tab_bar?.visibility = View.GONE
    }

    /**
     * 弹出聚合站点简介卡片，并根据电柜个数构建 Tab 栏。
     */
    private fun showNetStationView(group: StationGroup) {
        currentGroup = group
        net_station_view?.bindCtl(netStationCtl)
        // 地图模式：顶部直角 + 地址固定两行，避免圆角缺口与切换时高度跳动
        net_station_view?.setMapMode(true)
        // 默认展示第一个电柜
        showCabinet(group, 0)
        net_station_view?.visibility = View.VISIBLE
        iv_close_net_station_view?.visibility = View.VISIBLE

        // "简介"整体宽度 = 屏幕宽度 - 卡片左右各 10dp 边距
        val cardWidthPx = DensityUtil.getScreenWidth(requireContext()) -
                DensityUtil.dip2px(20f, requireContext())
        station_tab_bar?.setTabs(group.cabinets.size, 0, cardWidthPx) { index ->
            showCabinet(group, index)
        }
        station_tab_bar?.visibility = View.VISIBLE
    }

    private fun showCabinet(group: StationGroup, index: Int) {
        val cabinet = group.cabinets.getOrNull(index) ?: return
        net_station_view?.setNewData(cabinet)
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
        }
    }

    private var groups: MutableList<StationGroup> = mutableListOf()
    private fun getNetStationList(name: String = "") {
        lastRefreshDeviceId = FgtHome.CURRENT_DEVICEID
        dlgProgress = getSweetDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE, "请求中...")
        dlgProgress!!.show()
        http {
            url = "apiv6/cgstationnetwork/list"
            params["city_id"] = "000000"
            params["deviceId"] = FgtHome.CURRENT_DEVICEID
            params["name"] = name

            onSuccess { res ->
                rootView?.let {
                    val bean = NetStationGroupParser.parse(res)
                    aMap?.clear()
                    groups.clear()
                    markerMap.clear()
                    markGroupMap.clear()

                    val curV = FgtHome.getBatteryV()
                    bean.data.forEach { item ->
                        item.list.forEach { group ->
                            val cabinets = group.values.toList()
                            // 将父级城市ID传递到每个电柜，供 NetStationView 特例逻辑使用
                            cabinets.forEach { x -> x.cityId = item.city_id }
                            // 宜昌等特殊过滤，规则与之前一致
                            val filtered = filterGroupCabinets(item.city_id, cabinets, curV)
                            if (filtered.isNotEmpty()) {
                                groups.add(StationGroup(filtered))
                            }
                        }
                    }
                    showMarkList(name.isNotEmpty())
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

    private fun showMarkList(showFirstLocation: Boolean = false) {
        groups.forEach { group ->
            addMarker(group)
        }
        if (showFirstLocation) {
            if (groups.size > 0) {
                EasyToast.DEFAULT.show("已为您找到${groups.size}个站点")
                groups[0].let {
                    aMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.lat, it.lng)))
                    aMap?.moveCamera(CameraUpdateFactory.zoomTo(13f))
                }
            } else {
                EasyToast.DEFAULT.show("已为您找到0个站点")
            }
        } else {
            showPosInMap()
        }
    }

    private fun selectMarker(marker: Marker) {
        if (marker != null) {
            mCurrentMemMarker?.startAnimation()
            setNotClickedMarkerAnim()
            mCurrentMemMarker = marker
            marker?.startAnimation()
            setClickedMarkerAnim()
            val group = markGroupMap[marker.id] ?: return
            aMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(group.lat, group.lng)))
            aMap?.moveCamera(CameraUpdateFactory.zoomTo(15f))
            showNetStationView(group)
        }
    }

    private fun setNotClickedMarkerAnim() {
        if (mCurrentMemMarker != null) {
            var animation = ScaleAnimation(1.0f, 1.6f, 1.0f, 1.6f)
            animation.setDuration(0)
            animation.fillMode = 1
            mCurrentMemMarker?.setAnimation(animation)
        }
    }

    private fun setClickedMarkerAnim() {
        if (mCurrentMemMarker != null) {
            var animation = ScaleAnimation(1.6f, 1.0f, 1.6f, 1.0f)
            animation.setDuration(0)
            animation.fillMode = 1
            mCurrentMemMarker?.setAnimation(animation)
        }
    }

    private fun addMarker(group: StationGroup) {
        var imageUrl = "https://downxll.oss-cn-beijing.aliyuncs.com/lxhd/%s"
        imageUrl = if (group.isOnline == 1) {
            // 聚合可换电池数：按规则累加各电柜
            val ava = group.getAvaModelNum(FgtHome.getBatteryV())
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
                    addMarkerInfo(group, bitmap)
                }
            }
        }

    }

    private fun addMarkerInfo(group: StationGroup, markerBitmap: Bitmap) {
        var markerOption = MarkerOptions()
//            .zIndex(10f)
            .position(LatLng(group.lat, group.lng))
            .draggable(false)
        markerOption?.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
        var marker = aMap?.addMarker(markerOption)
        if (marker != null) {
            var animation = ScaleAnimation(1.0f, 1.6f, 1.0f, 1.6f)
            animation.setDuration(0)
            animation.fillMode = 1
            marker.setAnimation(animation)
            marker.isClickable = true
            group.markerId = marker.id
            markGroupMap[marker.id] = group
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