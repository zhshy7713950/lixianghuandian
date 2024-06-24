package com.ruimeng.things.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.TrajectoryPointsBean
import com.utils.TextUtil
import kotlinx.android.synthetic.main.fgt_trajectory.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.getTime
import wongxd.common.toPOJO
import wongxd.http
import java.util.*


/**
 * Created by wongxd on 2018/12/20.
 */
class FgtTrajectory : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_trajectory

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "轨迹")
        tv_select.setOnClickListener {
            showTimePicker {
                selectedData = it.time.getTime(false)
                selectedDateShow = it.time.getTime(false)
                selectedDateShow = selectedDateShow.replaceFirst("-","年").replaceFirst("-","月") + "日"
                tv_selected_date_trajectory.text =   TextUtil.getSpannableString(arrayOf("当前日期：",selectedDateShow),arrayOf("#929FAB","#FFFFFF"))

                getPoints()
            }
        }


        mMapview = mapView_trajectory
        mMapview?.onCreate(savedInstanceState)
        amap = mMapview?.map

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        selectedDateShow = "${year}年${month}月${day}日"
        selectedData = "${year}-${month}-${day}"

        tv_selected_date_trajectory.text =   TextUtil.getSpannableString(arrayOf("当前日期：",selectedDateShow),arrayOf("#929FAB","#FFFFFF"))

        getPoints()
    }

    private var selectedDateShow = ""
    private var selectedData = ""

    private fun showTimePicker(callback: (Date) -> Unit) {
        val selectedDate = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()

        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        //正确设置方式 原因：注意事项有说明
        startDate.set(year - 5, 1, 1)
        endDate.set(year , month, day)

        val pvTime = TimePickerBuilder(activity, object : OnTimeSelectListener {
            override fun onTimeSelect(date: Date, v: View?) {//选中事件回调
                callback(date)
            }
        })
            .setType(booleanArrayOf(true, true, true, false, false, false))// 默认全部显示
            .setCancelText("取消")//取消按钮文字
            .setSubmitText("确定")//确认按钮文字
            .setContentTextSize(18)//滚轮文字大小
            .setTitleSize(20)//标题文字大小
            .setTitleText("选择日期")//标题文字
            .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
            .isCyclic(true)//是否循环滚动
            .setTitleColor(Color.TRANSPARENT)//标题文字颜色
            .setSubmitColor(Color.parseColor("#29EBB6"))//确定按钮文字颜色
            .setCancelColor(Color.WHITE)//取消按钮文字颜色
            .setTitleBgColor(Color.parseColor("#404E59"))//标题背景颜色 Night mode
            .setBgColor(Color.parseColor("#404E59"))//滚轮背景颜色 Night mode
            .setTextColorCenter(Color.WHITE)
            .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
            .setRangDate(startDate, endDate)//起始终止年月日设定
            .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .isDialog(false)//是否显示为对话框样式
            .build()


        pvTime.show()
    }

    /**
     * 获取轨迹点
     */
    private fun getPoints() {

        if (selectedDateShow.isBlank()) {
            EasyToast.DEFAULT.show("请选择日期")
            return
        }

        http {
            url = Path.LBSHISTORICAL
            params["device_id"] = FgtHome.CURRENT_DEVICEID
            params["day"] = selectedData

            onSuccess {

//                tv_selected_date_trajectory.text = "当前日期:$selectedDate"

                val result = it.toPOJO<TrajectoryPointsBean>().data
                val latLngList: MutableList<com.amap.api.maps.model.LatLng> = mutableListOf()
                result.forEach {

                    val strs = it.lbs.split(",")
                    latLngList.add(com.amap.api.maps.model.LatLng(strs[1].toDouble(), strs[0].toDouble()))

                }

                drawPoints(latLngList)
            }
            onFail{ i: Int, s: String ->
                Log.i("TAG", "getPoints: "+s)
//                val latLngList: MutableList<LatLng> = mutableListOf()
//                latLngList.add(LatLng(30.54800,104.06398))
//                latLngList.add(LatLng(30.55303,104.06932))
//                latLngList.add(LatLng(30.55293,104.07994))
//                latLngList.add(LatLng(30.55678,104.06781))
//                latLngList.add(LatLng(30.56316,104.07878))
//                latLngList.add(LatLng(30.56316,104.07878))
//                drawPoints(latLngList)
            }
        }
    }

    private var mMapview: MapView? = null
    private var amap: AMap? = null

    private fun drawPoints(points: List<LatLng>) {

        amap?.clear()

        fun getBounds(pointlist: List<LatLng>): LatLngBounds {

            val b: LatLngBounds.Builder = LatLngBounds.builder()

            var i = 0
            while (i in 0 until pointlist.size) {
                b.include(pointlist[i])
                i++
            }

            return b.build()
        }

        // 获取轨迹坐标点

        val mpathSmoothTool = PathSmoothTool()
        //设置平滑处理的等级
        mpathSmoothTool.intensity = 4
        val pathoptimizeList = mpathSmoothTool.pathOptimize(points)
        //绘制轨迹，移动地图显示
        if (points.isNotEmpty()) {
            val mOriginPolyline = amap?.addPolyline(PolylineOptions().addAll(points).color(Color.GREEN))
            amap?.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(points), 200))
            addBatteryMarkder(points.last())
        }
    }


    private var markerOption: MarkerOptions? = null

    /**
     * 在地图上添加marker
     */
    private fun addBatteryMarkder(latLng: LatLng) {

        markerOption = MarkerOptions()
            .zIndex(10f)
            .position(latLng)
            .draggable(false)


        val v = View.inflate(activity, R.layout.layout_battery_trajectory_marker, null)
        val iv = v.findViewById<ImageView>(R.id.iv)

        markerOption?.icon(BitmapDescriptorFactory.fromView(v))

        amap?.addMarker(markerOption)

    }


    override fun onResume() {
        super.onResume()
        mMapview?.onResume()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapview?.onSaveInstanceState(outState)
    }


    override fun onPause() {
        mMapview?.onPause()
        super.onPause()
    }


    override fun onDestroyView() {
        mMapview?.onDestroy()
        super.onDestroyView()
    }
}