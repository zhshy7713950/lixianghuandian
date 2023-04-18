package com.ruimeng.things.home

import android.graphics.Color
import android.os.Bundle
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
        topbar.addRightImageButton(R.drawable.date_picker_trajectory, R.id.right).setOnClickListener { view ->
            showTimePicker {
                selectedDate = it.time.getTime(false)
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

        selectedDate = "$year-$month-$day"

        tv_selected_date_trajectory.text = "当前轨迹日期:$selectedDate"
        getPoints()
    }

    private var selectedDate = ""

    private fun showTimePicker(callback: (Date) -> Unit) {
        val selectedDate = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()

        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH) + 1
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        //正确设置方式 原因：注意事项有说明
        startDate.set(year - 1, 1, 1)
        endDate.set(year + 1, 1, 1)

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
            .setTitleColor(Color.BLACK)//标题文字颜色
            .setSubmitColor(Color.BLUE)//确定按钮文字颜色
            .setCancelColor(Color.BLUE)//取消按钮文字颜色
            .setTitleBgColor(-0x99999a)//标题背景颜色 Night mode
            .setBgColor(-0xcccccd)//滚轮背景颜色 Night mode
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

        if (selectedDate.isBlank()) {
            EasyToast.DEFAULT.show("请选择日期")
            return
        }

        http {
            url = Path.LBSHISTORICAL
            params["device_id"] = FgtHome.CURRENT_DEVICEID
            params["day"] = selectedDate


            onSuccess {

                tv_selected_date_trajectory.text = "当前轨迹日期:$selectedDate"

                val result = it.toPOJO<TrajectoryPointsBean>().data
                val latLngList: MutableList<com.amap.api.maps.model.LatLng> = mutableListOf()
                result.forEach {

                    val strs = it.lbs.split(",")
                    latLngList.add(com.amap.api.maps.model.LatLng(strs[1].toDouble(), strs[0].toDouble()))

                }

                drawPoints(latLngList)
            }
        }
    }

    private var mMapview: MapView? = null
    private var amap: AMap? = null

    private fun drawPoints(points: List<com.amap.api.maps.model.LatLng>) {

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