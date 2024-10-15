package com.ruimeng.things.net_station

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.batchat.preview.PreviewTools
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.net_station.bean.NetStationDetailBeanTwo
import com.ruimeng.things.net_station.view.AbsNetStationDetailCtl
import com.utils.unsafeLazy
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import kotlinx.android.synthetic.main.fgt_net_station_detail_new.net_station_detail_view
import kotlinx.android.synthetic.main.fgt_net_station_detail_new.rv_battery
import kotlinx.android.synthetic.main.fgt_net_station_detail_new.station_banner
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.dissmissProgressDialog
import wongxd.common.showProgressDialog
import wongxd.common.toPOJO
import wongxd.http

class FgtNetStationDetailNew : BaseBackFragment() {

    private val stationId by lazy { arguments?.getString("stationId") ?: "" }
    private val netStationDetailCtl by unsafeLazy {
        NetStationDetailCtlImpl()
    }
    private val rvAdapter by lazy { RvAdapter() }
    override fun getLayoutRes(): Int = R.layout.fgt_net_station_detail_new

    companion object {
        fun newInstance(stationId: String): FgtNetStationDetailNew {
            return FgtNetStationDetailNew().apply {
                arguments = Bundle().apply {
                    putString("stationId", stationId)
                }
            }
        }
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "电柜详情")
        rv_battery.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = rvAdapter
        }
        getInfo()
    }

    private fun getInfo() {
        showProgressDialog()
        http {
            url = "apiv4/cgstationinfo"
            params["id"] = stationId
            onFinish {
                dissmissProgressDialog()
            }

            onFail { _, msg ->
                EasyToast.DEFAULT.show(msg)
            }

            onSuccess { res ->
                val bean = res.toPOJO<NetStationDetailBeanTwo>().data
                with(net_station_detail_view){
                    isVisible = true
                    setNewData(bean)
                    bindCtl(netStationDetailCtl)
                }
                rvAdapter.swCabSocControl = bean.swCabSocControl.toIntOrNull() ?: 80
                if (bean.exchange.isNotEmpty() && bean.exchange[0].device.isNotEmpty()) {
                    rvAdapter.setNewData(bean.exchange[0].device)
                }
                if (bean.site_image.isNullOrEmpty() || bean.site_image.all { it.isNullOrEmpty() }) {
                    station_banner.isVisible = false
                } else {
                    station_banner.isVisible = true
                    val dataList = bean.site_image.filter { it.isNullOrEmpty().not() }
                    (station_banner as Banner<String, BannerImageAdapterImpl>)
                        .setAdapter(BannerImageAdapterImpl(dataList).apply {
                            this.setOnBannerListener { _, position ->
                                PreviewTools.startImagePreview(this@FgtNetStationDetailNew.requireActivity(), ArrayList(dataList), station_banner, position)
                            }
                        }, true)
                        .addBannerLifecycleObserver(this@FgtNetStationDetailNew)
                        .indicator = CircleIndicator(activity)

                }
            }

            onFail { _, msg ->
                EasyToast.DEFAULT.show(msg)
            }

        }
    }

    inner class BannerImageAdapterImpl(private val dataList: List<String>) : BannerImageAdapter<String>(dataList) {
        override fun onBindView(
            holder: BannerImageHolder,
            data: String,
            position: Int,
            size: Int
        ) {
            Glide.with(holder.itemView)
                .load(data)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(10)))
                .into(holder.imageView)
        }
    }

    inner class RvAdapter :
        BaseQuickAdapter<NetStationDetailBeanTwo.Data.ExchangeBean.DeviceBean, BaseViewHolder>(R.layout.item_rv_net_station_detail_new) {

        var swCabSocControl: Int = 80

        @SuppressLint("SetTextI18n")
        override fun convert(
            helper: BaseViewHolder,
            bean: NetStationDetailBeanTwo.Data.ExchangeBean.DeviceBean
        ) {
            bothNotNull(helper, bean) { h, b ->
                //默认状态设置
                h.setVisible(R.id.tv_battery_level, false)
                h.setVisible(R.id.iv_battery_type, false)
                h.setTextColor(R.id.tv_battery_status, Color.parseColor("#B2C1CE"))
                h.setAlpha(R.id.cl_container, 0.5f)
                with(b) {
                    h.setText(R.id.tv_id, id)
                    if (status == 0 || status == 1) {
                        when (device_type) {
                            "72伏" -> {
                                h.setImageResource(
                                    R.id.iv_battery_type,
                                    R.drawable.ic_model_72_large
                                )
                                h.setVisible(R.id.iv_battery_type, true)
                            }

                            "60伏" -> {
                                h.setImageResource(
                                    R.id.iv_battery_type,
                                    R.drawable.ic_model_60_large
                                )
                                h.setVisible(R.id.iv_battery_type, true)
                            }

                            "48伏" -> {
                                h.setImageResource(
                                    R.id.iv_battery_type,
                                    R.drawable.ic_model_48_large
                                )
                                h.setVisible(R.id.iv_battery_type, true)
                            }

                            else -> {
                                h.setVisible(R.id.iv_battery_type, false)
                            }
                        }
                        h.setText(R.id.tv_battery_level, "${electricity}%")
                        h.setVisible(R.id.tv_battery_level, true)
                        val electricityInt = electricity.toIntOrNull()
                        when (electricityInt) {
                            in 91..100 -> h.setImageResource(
                                R.id.iv_battery,
                                R.drawable.ic_battery_full
                            )

                            in 81..90 -> h.setImageResource(
                                R.id.iv_battery,
                                R.drawable.ic_battery_90
                            )

                            in 61..80 -> h.setImageResource(
                                R.id.iv_battery,
                                R.drawable.ic_battery_80
                            )

                            in 41..60 -> h.setImageResource(
                                R.id.iv_battery,
                                R.drawable.ic_battery_60
                            )

                            in 21..40 -> h.setImageResource(
                                R.id.iv_battery,
                                R.drawable.ic_battery_40
                            )

                            else// <=20
                            -> h.setImageResource(R.id.iv_battery, R.drawable.ic_battery_20)
                        }
                        if (FgtHome.getBatteryV()
                                .isNullOrEmpty() || !device_type.startsWith(FgtHome.getBatteryV())
                        ) {
                            h.setText(R.id.tv_battery_status, "电池型号不匹配")
                        } else {
                            if (electricityInt != null && electricityInt >= swCabSocControl) {
                                h.setText(R.id.tv_battery_status, "正常可换")
                                h.setTextColor(R.id.tv_battery_status, Color.WHITE)
                                h.setAlpha(R.id.cl_container, 1f)
                            } else {
                                h.setText(R.id.tv_battery_status, "充电中")
                            }
                        }
                    } else if (status == 2) {// 无电池
                        h.setImageResource(R.id.iv_battery, R.drawable.ic_battery_empty)
                        h.setText(R.id.tv_battery_status, "空仓")
                    } else if (status == 9) {// 故障
                        h.setImageResource(R.id.iv_battery, R.drawable.ic_battery_fault)
                        h.setText(R.id.tv_battery_status, "故障")
                    } else {// 未知
                        h.setImageResource(R.id.iv_battery, R.drawable.ic_battery_unknown)
                        h.setText(R.id.tv_battery_status, "未知")
                    }

                    if (lockStatus == 0) {// 禁用
                        h.setText(R.id.tv_battery_status, "该仓已禁用")
                    }
                }
            }
        }
    }

    inner class NetStationDetailCtlImpl : AbsNetStationDetailCtl() {
        override fun onUpdateClick() {
            getInfo()
        }
    }
}