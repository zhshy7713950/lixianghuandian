package com.ruimeng.things.net_station

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean
import com.utils.BitmapUtil
import com.utils.CommonUtil
import com.utils.DensityUtil
import com.utils.GlideHelper
import com.utils.TextUtil
import com.zhihu.matisse.engine.impl.GlideEngine
import kotlinx.android.synthetic.main.fgt_net_station_by_map.*
import kotlinx.android.synthetic.main.layout_station_infowindow.layout_bottom_info
import org.greenrobot.eventbus.EventBus
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
        layout_bottom_info.visibility = View.GONE
    }

    override fun pop() {
        EventBus.getDefault().post(FgtNetStationItem.RefreshStationList())
        super.pop()
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
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(13f))

        aMap?.setOnMarkerClickListener {
            selectMarker(it)

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
            aMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(agent!!.lat,agent.lng)))
            aMap?.moveCamera(CameraUpdateFactory.zoomTo(15f))
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
                showBottomStationInfo(agent)
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
                params["appType"] = "lxhd"

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
//        zoomToSpan()
        selectMarker?.let {
            selectMarker(it)
        }


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

//        if(list.isNotEmpty()){
//            var nearestDistance = 0f
//            var nearestPoint = LatLng(list[0].lat, list[0].lng)
//                list.forEach {
//                    val distance = AMapUtils.calculateLineDistance(
//                        LatLng(mCurrentLat, mCurrentLon),
//                        LatLng(it.lat, it.lng)
//                    )
//
//                    if (nearestDistance > distance || nearestDistance == 0f) {
//                        nearestPoint = LatLng(it.lat, it.lng)
//                        nearestDistance = distance
//                    }
//                }
//            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(nearestPoint, 13f))
//        }else{
            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mCurrentLat, mCurrentLon), 13f))
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
//        var latLng = LatLng(mCurrentLat, mCurrentLon)
//        if (!list.isEmpty()){
//            latLng = LatLng(list[0].lat, list[0].lng)
//        }
//        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))

    }

    var selectMarker:Marker? = null
    var bitMaps :  MutableList<Bitmap> = mutableListOf()
    /**
     * 在地图上添加marker
     */
    private fun addMarker(agent:  NetStationBean.Data.X) {


//        var markerBitmap: Bitmap = null
        if(getType == "3") {
            var imageUrl = "https://downxll.oss-cn-beijing.aliyuncs.com/lxhd/%s"
            if (agent.isOnline == 1){
                imageUrl =  String.format(imageUrl,String.format("mapballoon-change-%s-%s-%s@3x.png","small", if(agent.isGreen == 1 ) "green" else "yellow",agent.available_battery))
//                imageUrl =  String.format(imageUrl,"mapballoon-change-large-offline@3x.png")

//                markerBitmap =
//                    context?.let { BitmapUtil().overlayTextOnImage(it,
//                        if(agent.isGreen == 1 ) R.mipmap.ic_map_marker_small_2 else R.mipmap.ic_map_marker_small_1 ,
//                        agent.available_battery,
//                        if(agent.isGreen == 1 )  Color.parseColor("#29EBB6") else Color.parseColor("#FEB41E")) }!!

            }else{
                imageUrl =  String.format(imageUrl,"mapballoon-change-small-offline@3x.png")
//                markerBitmap = context?.let {
//                    BitmapFactory.decodeResource(
//                        context!!.resources,
//                        R.mipmap.ic_map_marker_off_line
//                    )
//                }!!
            }
            Log.i("TAG", "addMarker: "+imageUrl)
            context?.let {
                GlideHelper.loadImageAsBitmap(it,imageUrl){
                    bitmap ->
                    if (bitmap != null){
                        addMarkerInfo(agent, bitmap)
                    }
                }
//                Glide.with(it)
//                    .asBitmap()
//                    .load(imageUrl)
//                    .into(object :SimpleTarget<Bitmap>(){
//                        override fun onResourceReady(
//                            resource: Bitmap,
//                            transition: Transition<in Bitmap>?
//                        ) {
//                            addMarkerInfo(agent, resource)
//                        }
//                    })
            }


        }else {
           var markerBitmap = context?.let {
                    BitmapFactory.decodeResource(
                        context!!.resources,
                        R.mipmap.service_station_small
                    )
                }!!
            addMarkerInfo(agent, markerBitmap)
        }

    }
    private fun addMarkerInfo(agent: NetStationBean.Data.X,markerBitmap:Bitmap){
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
                    a.setText(R.id.tv_count,"${b.available_battery}")
                        .setGone(R.id.tv_count,true)
                        .setGone(R.id.tv_count_title,true)
                            if (b.isOnline == 1){
                                a.setTextColor(R.id.tv_count,if (b.isGreen == 1) Color.parseColor("#29EBB6") else Color.parseColor("#FEB41E"))
                            }else{
                                a.setTextColor(R.id.tv_count,Color.parseColor("#D5D5D5"))
                                    .setText(R.id.tv_count,"0").
                                    setText(R.id.tv_count_title,"设备已离线")
                            }
                    a.setGone(R.id.iv01,false)
                }else{
                    a.setGone(R.id.iv01,true)
                        .setGone(R.id.tv_count,false)
                        .setGone(R.id.tv_count_title,false)
                }
                a.setText(R.id.tv_number,"${b.tel}")
                    .setText(R.id.text2,"电话：")
                a.itemView.setOnClickListener{
                    var marker = markerMap[b.markerId]
                    if (marker != null){
                        marker.zIndex = 15f
                        selectMarker(marker)
                        var latLng = LatLng(b.lat,b.lng)
                        aMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        aMap?.moveCamera(CameraUpdateFactory.zoomTo(15f))
                    }
                }
                var parentView = helper.getView<ConstraintLayout>(R.id.cl_content)
                if (helper.layoutPosition == 0){
                    parentView.setPadding(0, DensityUtil.dip2px(30f,context).toInt(),0,0)
                }else{
                    parentView.setPadding(0, DensityUtil.dip2px(10f,context).toInt(),0,0)
                }

            }
        }

    }

    private fun showBottomStationInfo(agent: NetStationBean.Data.X) {
        layout_bottom_info.visibility = View.VISIBLE
        val v = layout_bottom_info
        val ivClose = v.findViewById<ImageView>(R.id.iv_close)
        val tvStationName = v.findViewById<TextView>(R.id.tv_station_name)
        val tvStationPhone = v.findViewById<TextView>(R.id.tv_station_phone)
        val tvStationDistance = v.findViewById<TextView>(R.id.tv_station_distance)
        val tvStationLocation = v.findViewById<TextView>(R.id.tv_station_location)
        val tvStationTag = v.findViewById<TextView>(R.id.tv_station_tag)
        val tvStationBatteryCount = v.findViewById<TextView>(R.id.tv_station_battery_count)
        val tv_phone_call = v.findViewById<TextView>(R.id.tv_phone_call)
        val tv_agent_code = v.findViewById<TextView>(R.id.tv_agent_code)
        val tv_in_shop = v.findViewById<TextView>(R.id.tv_in_shop)
        val tv_count = v.findViewById<TextView>(R.id.tv_count)
        val tv_count_title = v.findViewById<TextView>(R.id.tv_count_title)

        if (getType == "3") {
            tv_phone_call.text = "联系经销商"
            tv_agent_code.visibility = View.GONE
            tvStationBatteryCount.visibility = View.GONE
            tv_in_shop.visibility = View.VISIBLE
            tv_count.visibility = View.VISIBLE

            if (agent.isOnline == 1) {
                tv_count.setTextColor(
                    if (agent.isGreen == 1) Color.parseColor("#29EBB6") else Color.parseColor(
                        "#FEB41E"
                    )
                )
                tv_count.text = agent.available_battery
                tv_count_title.text = "可换电池数"
            } else {
                tv_count.setTextColor(Color.parseColor("#D5D5D5"))
                tv_count.text = "0"
                tv_count_title.text = "设备已离线"
            }
            tv_count_title.visibility = View.VISIBLE
            v.findViewById<ImageView>(R.id.iv01).visibility = View.GONE

        } else {
            tv_phone_call.text = "立即联系"
            tv_agent_code.visibility = View.VISIBLE
            var agentTag =
                if (agent.tag.startsWith("代理编码")) agent.tag.substring(5) else agent.tag
            tv_agent_code.text = TextUtil.getSpannableString(arrayOf("代理编码：", agentTag))
            tvStationBatteryCount.visibility = View.VISIBLE
            tvStationBatteryCount.text =
                TextUtil.getSpannableString(arrayOf("可租电池数：", "${agent.count}"))
            tv_in_shop.visibility = View.GONE
            tv_count.visibility = View.GONE
            tv_count_title.visibility = View.GONE
            v.findViewById<ImageView>(R.id.iv01).visibility = View.VISIBLE
        }
        tv_in_shop.setOnClickListener {
            if ("3" == getType) {
                start(FgtNetStationDetailTwo.newInstance(agent.site_name, agent.id))
            }
        }

        v.setOnClickListener {  }
        ivClose.setOnClickListener {
            layout_bottom_info.visibility = View.GONE
        }
        val distance =
            AMapUtils.calculateLineDistance(LatLng(agent.lat, agent.lng), LatLng(App.lat, App.lng))
        val distanceStr =
            if (distance >= 1000)
                "${String.format("%.2f", (distance / 1000))}公里"
            else
                "${String.format("%.2f", distance)}米"


        tvStationName.text = agent.site_name
        tvStationPhone.text = agent.tel
        tvStationDistance.text = "距离我${distanceStr}"
        tvStationLocation.text = agent.address
        tvStationTag.text = agent.tag
        v.findViewById<ImageView>(R.id.iv01)
            .setImageResource(if (getType == "3") R.mipmap.marker_net_station_big else R.mipmap.service_station_big)

        v.findViewById<TextView>(R.id.tv_phone_call).setOnClickListener {
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
        }
//        tvStationBatteryCount.visibility = if (type == "3") View.VISIBLE else View.GONE
//        tvStationBatteryCount.text = "${agent.count}台"

//        v.findViewById<TextView>(R.id.tv_in_shop).setOnClickListener {
//            dismiss()
//            markCallback.click(it, agent)
//        }

        v.findViewById<TextView>(R.id.tv_navi_here).setOnClickListener {
            CommonUtil.naviToLocation(activity!!, agent.lat, agent.lng, agent.site_name)
        }

    }

}