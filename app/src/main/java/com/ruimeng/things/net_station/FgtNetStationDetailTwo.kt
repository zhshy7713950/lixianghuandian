package com.ruimeng.things.net_station

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationDetailBeanTwo
import com.utils.CommonUtil
import kotlinx.android.synthetic.main.fgt_net_station_detail_two.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sp
import org.w3c.dom.Text
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.getCurrentAppAty
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.SystemUtils
import java.util.Timer
import java.util.TimerTask


class FgtNetStationDetailTwo : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_net_station_detail_two

    private var getRecommendId = 0
    private var getRecommendPos = 0
    private var timer = Timer()

    companion object {
        fun newInstance(title: String, stationId: String): FgtNetStationDetailTwo {
            return FgtNetStationDetailTwo().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("stationId", stationId)
                }
            }
        }
    }

    private val title by lazy { arguments?.getString("title") ?: "" }
    private val stationId by lazy { arguments?.getString("stationId") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, title)
        recyclerview.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = rvAdapter
        }
        val task = object : TimerTask(){
            override fun run() {
                getInfo()
            }
        }
        timer.schedule(task,0,30000)
    }

    private var currentIndex = 0
    @SuppressLint("SetTextI18n")
    private fun getInfo() {

        http {


            url = "apiv4/cgstationinfo"
            params["id"] = stationId


            onFinish {

            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }

            onSuccess { res ->
                val bean = res.toPOJO<NetStationDetailBeanTwo>().data
                rvAdapter.removeAllHeaderView()
                var header = View.inflate(activity, R.layout.layout_batter_header, null)
                header.findViewById<TextView>(R.id.addressDetailText).setText("${bean.address}")
                header.findViewById<TextView>(R.id.tvCode).setText("${bean.code}")
                header.findViewById<TextView>(R.id.tvBatteryNum).setText("电池数量：${bean.device_num}")
                header.findViewById<TextView>(R.id.tvNumber).setText("可换电池数量：${bean.device_available_num}")
                rvAdapter.addHeaderView(header)
                rvAdapter.notifyDataSetChanged()
                header.findViewById<TextView>(R.id.tv_call).setOnClickListener {
                    AnyLayer.with(getCurrentAppAty())
                        .contentView(R.layout.alert_phone_call_dialog)
                        .bindData { anyLayer ->
                            anyLayer.contentView.findViewById<TextView>(R.id.tvTitle).setText(bean.tel)
                            anyLayer.contentView.findViewById<TextView>(R.id.tv_name).setText(bean.site_name)
                            anyLayer.contentView.findViewById<View>(R.id.fl_call).setOnClickListener{
                                getPermissions(activity, PermissionType.CALL_PHONE, allGranted = {
                                    SystemUtils.call(context, bean.tel)
                                })
                                anyLayer.dismiss()
                            }
                            anyLayer.contentView.findViewById<ImageView>(R.id.ivClose).setOnClickListener{
                                anyLayer.dismiss()
                            }
                        }.backgroundColorInt(Color.parseColor("#85000000"))
                        .backgroundBlurRadius(10f)
                        .backgroundBlurScale(10f)
                        .show()
                }
                header.findViewById<TextView>(R.id.tv_nav).setOnClickListener{
                    CommonUtil.naviToLocation(activity!!,bean.lat,bean.lng, bean.site_name)
                }

                val dataList = ArrayList<NetStationDetailBeanTwo.Data.ExchangeBean>()
                dataList.clear()
                dataList.addAll(bean.exchange)
                if (!bean.exchange.isEmpty() && !bean.exchange[0].device.isEmpty()){
                    rvAdapter.setNewData(bean.exchange[0].device)
                }



            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }

        }

    }

    private val rvAdapter by lazy { RvAdapter() }


    inner class RvAdapter :
        BaseQuickAdapter<NetStationDetailBeanTwo.Data.ExchangeBean.DeviceBean, BaseViewHolder>(R.layout.item_rv_net_station_detail_2) {
        @SuppressLint("SetTextI18n")
        override fun convert(
            helper: BaseViewHolder,
            bean: NetStationDetailBeanTwo.Data.ExchangeBean.DeviceBean
        ) {
            bothNotNull(helper, bean) { a, b ->
                var status = listOf("0","1","2","3","4")
                var statusTxt = listOf("正常可换","充电中","无电池","故障","未知")
                var colors = listOf("#15EF9B","#29EBB6","#A8B9B3","#F78E6B","#67A7EC")
                var resources = listOf(R.mipmap.battery_image_1,R.mipmap.battery_image_2,R.mipmap.battery_image_4,R.mipmap.battery_image_3,R.mipmap.battery_image_5)

                var index = status.indexOf(b.status)
                index = if (index == -1) 4 else index

                a.setText(R.id.positionText,(b.pos.toInt() + 1).toString())
                    .setText(R.id.tvPercent,if(index < 2)  "${b.electricity}%" else "0%")
                    .setTextColor(R.id.tvPercent,Color.parseColor(colors[index]))
                    .setImageResource(R.id.imageView,resources[index])
                    .setText(R.id.tvCode,"${b.device_id}")
                    .setText(R.id.tvStatus,statusTxt[index])


            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

}