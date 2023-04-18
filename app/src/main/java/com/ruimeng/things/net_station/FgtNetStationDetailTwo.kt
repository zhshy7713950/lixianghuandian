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
import kotlinx.android.synthetic.main.fgt_net_station_detail_two.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sp
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.toPOJO
import wongxd.http


class FgtNetStationDetailTwo : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_net_station_detail_two

    private var getRecommendId = 0
    private var getRecommendPos = 0

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
            layoutManager = GridLayoutManager(activity, 4)
            adapter = rvAdapter
        }
        getInfo()
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

                addressDetailText?.text = "地址：${bean.address}"
                numberText?.text = "电池数量：${bean.device_num}    可换电数量：${bean.device_available_num}"
                recommendText?.text = bean.recommend_str
                getRecommendId =bean.recommend_id.toInt()
                getRecommendPos = bean.recommend_pos.toInt()
                currentIndex=getRecommendId
                val fragmentList = ArrayList<BaseBackFragment>()
                val titleList = ArrayList<String>()
                val dataList = ArrayList<NetStationDetailBeanTwo.Data.ExchangeBean>()
                dataList.clear()
                dataList.addAll(bean.exchange)

                if (dataList.isNotEmpty()) {
                    fragmentList.clear()
                    titleList.clear()

                    for (i in dataList.indices) {
                        titleList.add(dataList[i].name)
                    }


                    mQMUITabSegment?.apply {
                        reset()
                        for (i in 0 until titleList.size) {
                            addTab(QMUITabSegment.Tab(titleList[i]))
                        }
                        setTabTextSize(sp(12))
                        setHasIndicator(true)
                        setIndicatorWidthAdjustContent(true)
                        setDefaultNormalColor(resources.getColor(R.color.text_color))
                        setDefaultSelectedColor(resources.getColor(R.color.black))
                        setOnTabClickListener { index ->
                            currentIndex = index
                            val list =
                                ArrayList<NetStationDetailBeanTwo.Data.ExchangeBean.DeviceBean>()
                            list.clear()
                            rvAdapter.setNewData(list)
                            rvAdapter.setNewData(dataList[currentIndex].device)
                        }
                        selectTab(currentIndex)
                        notifyDataChanged()
                    }

                    rvAdapter.setNewData(dataList[currentIndex].device)
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

                if ((currentIndex==getRecommendId)&&(getRecommendPos==a.adapterPosition)){
                    a.getView<FrameLayout>(R.id.checkedLayout)?.apply {
                        visibility= View.VISIBLE
                    }

                }else{
                    a.getView<FrameLayout>(R.id.checkedLayout)?.apply {
                        visibility= View.GONE
                    }
                }
                val getStatus =
                    when (b.status) {
                        "0" -> {
                            "状态：可换"
                        }
                        "1" -> {
                            "状态：充电"
                        }
                        "2" -> {
                            "状态：无电池"
                        }
                        "9" -> {
                            "状态：故障"
                        }
                        else -> {
                            "状态："
                        }
                    }
                a.getView<LinearLayout>(R.id.rootLayout)?.apply {
                    //插槽状态 0正常可换1充电中2无电池9故障
                    when (b.status) {
                        "0" -> {
                            backgroundColor = Color.parseColor("#83C68E")
                        }
                        "1" -> {
                            backgroundColor = Color.parseColor("#F8BE4F")
                        }
                        "2" -> {
                            backgroundColor = Color.parseColor("#B1B1B1")
                        }
                        "9" -> {
                            backgroundColor = Color.parseColor("#B1B1B1")
                        }
                    }
                }
                a.getView<ImageView>(R.id.imageView)?.apply {
                    //插槽状态 0正常可换1充电中2无电池9故障
                    when (b.status) {
                        "0" -> {
                            backgroundResource = R.mipmap.battery_image_1
                        }
                        "1" -> {
                            backgroundResource = R.mipmap.battery_image_2
                        }
                        "2" -> {
                            backgroundResource = R.mipmap.battery_image_3
                        }
                        "9" -> {
                            backgroundResource = R.mipmap.battery_image_3
                        }
                    }
                }


                a.getView<TextView>(R.id.positionText)
                    ?.apply {
                        text = (b.pos.toInt() + 1).toString()
                    }

                a.getView<TextView>(R.id.textView)?.apply {
                    text = "编号：${b.device_id}\n电量：${b.electricity}%\n${getStatus}"
                }

            }
        }
    }

}