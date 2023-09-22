package com.ruimeng.things.net_station

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean
import com.utils.CommonUtil
import kotlinx.android.synthetic.main.fgt_net_station_by_map.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.SystemUtils

/**
 * Created by wongxd on 2019/7/3.
 */
class FgtNetStationByMap : BaseBackFragment() {
    companion object {
        fun newInstance(type: String = "",name:String = "",id:String = "0", locations: ArrayList<NetStationBean.Data.X>): FgtNetStationByMap {
            val fgt = FgtNetStationByMap()
            val b = Bundle()
            b.putString("type", type)
            b.putString("name",name)
            b.putString("id",id)
            b.putParcelableArrayList("list",locations)
            fgt.arguments = b
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_net_station_by_map
    private val getType by lazy { arguments?.getString("type", "") ?: "" }
    private val name by lazy { arguments?.getString("name", "") ?: "" }
    private val id by lazy { arguments?.getString("id", "") ?: "" }
    private var mCurrentMemMarker: Marker? = null
    private val markInfoMap: MutableMap<String, NetStationBean.Data.X> = mutableMapOf()
    private val markerMap: MutableMap<String, Marker> = mutableMapOf()

    private var markerOption: MarkerOptions? = null

    private val bitmap by lazy {  var iv = ImageView(activity)
        iv.setImageResource(if(getType == "3") R.mipmap.marker_net_station_small else R.mipmap.service_station_small)
        BitmapDescriptorFactory.fromView(iv)
    }
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, if(getType == "3") "换电地图查找" else "售后服务地图查找")
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
    private val stationAdapter by lazy { StationRvAdapter() }


    private fun afterGetPermission(savedInstanceState: Bundle?) {

        rvStation.layoutManager = LinearLayoutManager(activity)
        rvStation.adapter = stationAdapter

        mMapView = rootView?.findViewById(R.id.mapView_nearby) as MapView?
        mMapView?.onCreate(savedInstanceState) // 此方法必须重写


        if (aMap == null) {
            aMap = mMapView?.map
        }

        //设置希望展示的地图缩放级别
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(15f))

        aMap?.setOnMarkerClickListener {
            selectMarker(it)
            aMap?.moveCamera(CameraUpdateFactory.zoomTo(15f))
            false
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
            if (agent != null){
                showInfoPop(agent)
            }
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
    private fun setNotClickedMarkerAnim(){
        if (mCurrentMemMarker != null){
            var animation = ScaleAnimation(1.0f,1.6f,1.0f,1.6f)
            animation.setDuration(0)
            animation.fillMode = 1
            mCurrentMemMarker?.setAnimation(animation)
        }
    }


    private fun showInfoPop(agent:NetStationBean.Data.X){
         activity?.let {
             if (agent != null){
                 MarkPopupWindow(it,agent,getType,object :MarkPopupWindow.OnMarKCallback{
                     override fun click(view: View,agent:NetStationBean.Data.X) {
                         if (view.id == R.id.tv_phone_call){
                             NormalDialog(activity).apply {
                                 style(NormalDialog.STYLE_TWO)
                                 title("${agent.tel}")
                                 titleTextColor(Color.parseColor("#131414"))
                                 btnTextColor(Color.parseColor("#131414"), Color.GRAY)
                                 btnText("立即拨打", "取消")
                                     .setOnBtnClickL(OnBtnClickL {
                                         getPermissions(activity,
                                             PermissionType.CALL_PHONE,
                                             allGranted = { SystemUtils.call(activity, agent.tel) })
                                         dismiss()
                                     }, OnBtnClickL {
                                         dismiss()
                                     })
                                 show()
                             }
                         }else if (view.id == R.id.tv_in_shop){
                             if ("3"==getType){
                                 start(FgtNetStationDetailTwo.newInstance(agent.site_name, agent.id))
                             }else{
                                 start(FgtNetStationDetail.newInstance(agent.site_name, agent.id))
                             }
                         }else if (view.id == R.id.tv_navi_here){
                             CommonUtil.naviToLocation(activity!!,agent.lat,agent.lng, agent.site_name)
                         }
                     }
                 })
             }
        }
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
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
//        myLocationStyle.interval(2000) //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。

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

    private var locations: MutableList<NetStationBean.Data.X> = mutableListOf()
    private fun getList() {
        locations = arguments?.getParcelableArrayList<NetStationBean.Data.X>("list")!!
        if (!locations.isNullOrEmpty()){
            showMarkList()
        }else{
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
                params["name"] = name

                onSuccess { res ->
                    rootView?.let {
                        val data = res.toPOJO<NetStationBean>().data
                        data.forEach { item ->
                            locations.addAll(item.list)
                        }
                        showMarkList()

                    }
                }
                onFail { i, s ->
                    EasyToast.DEFAULT.show(s)
                }
            }
        }

    }
    fun showMarkList(){
        locations.forEach { loc ->
            addMarker(loc)
            pointList.add(LatLng(loc.lat,loc.lng))
        }
        setSuitZoom(locations)
        stationAdapter.setNewData(locations)
        selectMarker?.let { selectMarker(it) }
        zoomToSpan()
    }
    var pointList = ArrayList<LatLng>()
    fun zoomToSpan() {
        if (pointList != null && pointList.size > 0) {
            if (aMap == null) return
            val bounds: LatLngBounds = getLatLngBounds(pointList)
            aMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
        }
    }

    /**
     * 根据自定义内容获取缩放bounds
     */
    private fun getLatLngBounds(pointList: List<LatLng>): LatLngBounds {
        val b = LatLngBounds.builder()
        for (i in pointList.indices) {
            val p = pointList[i]
            b.include(p)
        }
        return b.build()
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

//        var nearestDistance = 0f
//        list.forEach {
//            val distance = AMapUtils.calculateLineDistance(
//                LatLng(mCurrentLat, mCurrentLon),
//                LatLng(it.lat, it.lng)
//            )
//
//            if (nearestDistance > distance || nearestDistance == 0f) {
//                nearestDistance = distance
//            }
//
//        }
//
//        val zoomSize =
//            when (nearestDistance) {
//                in 10..100 -> 18f
//                in 100..500 -> 15f
//                in 500..1000 -> 14f
//                in 1000..2000 -> 13f
//                in 2000..5000 -> 12f
//                in 1000..10000 -> 11f
//                in 1000..20000 -> 10f
//                in 1000..30000 -> 9f
//                in 1000..50000 -> 8f
//                in 1000..100000 -> 7f
//                in 100000..1000000 -> 5f
//                else -> 17f
//            }
        var latLng = LatLng(mCurrentLat, mCurrentLon)
        if (!list.isEmpty()){
            latLng = LatLng(list[0].lat, list[0].lng)
        }
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))

    }

    var selectMarker:Marker? = null

    /**
     * 在地图上添加marker
     */
    private fun addMarker(agent: NetStationBean.Data.X) {
        markerOption = MarkerOptions()
            .zIndex(10f)
            .position(LatLng(agent.lat, agent.lng))
            .draggable(false)

        markerOption?.icon(bitmap)

        var marker = aMap?.addMarker(markerOption)
        if (marker != null){
            var animation = ScaleAnimation(1.0f,1.6f,1.0f,1.6f)
            animation.setDuration(0)
            animation.fillMode = 1
            marker.setAnimation(animation)
            marker.isClickable = true
            agent.markerId = marker.id
            markInfoMap.put(marker.id, agent)
            markerMap.put(marker.id,marker)
            if (id == agent.id){
                selectMarker = marker
            }
        }

    }

    inner class StationRvAdapter :
        BaseQuickAdapter<NetStationBean.Data.X, BaseViewHolder>(R.layout.item_rv_map_station) {
        override fun convert(helper: BaseViewHolder, item: NetStationBean.Data.X?) {
            bothNotNull(helper, item) { a, b ->
                val distance = AMapUtils.calculateLineDistance(LatLng(b.lat, b.lng), LatLng(App.lat, App.lng))
                val distanceStr = if (distance >= 1000)
                        "${String.format("%.2f", (distance / 1000))}公里"
                    else
                        "${String.format("%.2f", distance)}米"
                a.setText(R.id.tv_title,b.site_name)
                    .setText(R.id.tv_distance,"距离我${distanceStr}")
                    .setText(R.id.tv_location,"${b.address}")
                    .setImageResource(R.id.iv01,if(getType == "3")  R.mipmap.marker_net_station_big else R.mipmap.service_station_big)
                if (getType == "3"){
                    a.setText(R.id.tv_number,"${b.count}")
                        .setText(R.id.text2,"可换电池数：")
                }else{
                    a.setText(R.id.tv_number,"${b.tel}")
                        .setText(R.id.text2,"电话：")
                }
                a.itemView.setOnClickListener{
                    var marker = markerMap[b.markerId]
                    if (marker != null){
                        marker.zIndex = 15f
                        selectMarker(marker)
                        var latLng = LatLng(b.lat,b.lng)
                        aMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    }
                }

            }
        }

    }
}