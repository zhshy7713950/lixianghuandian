package com.ruimeng.things.net_station

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationDetailBean
import kotlinx.android.synthetic.main.fgt_net_station_detail.*
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


class FgtNetStationDetail : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_net_station_detail

    companion object {
        fun newInstance(title: String, stationId: String): FgtNetStationDetail {
            return FgtNetStationDetail().apply {
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

        srl_net_station_detail?.apply {
            setOnRefreshListener {
                page = 1
                getInfo()
            }


            setOnLoadMoreListener {
                getInfo()
            }
        }

        rv_net_station_detail.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = rvAdapter.apply {
                setEmptyView(R.layout.layout_empty, rv_net_station_detail)
            }
        }

        getInfo()
    }


    private var page = 1
    private var pageSize = 10

    private fun getInfo() {

        http {


            url = PathV3.NETWORK_INFO
            params["id"] = stationId
            params["page"] = page.toString()
            params["page_size"] = pageSize.toString()


            onFinish {
                srl_net_station_detail?.apply {
                    finishRefresh()
                    finishLoadMore()
                }
            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }

            onSuccess { res ->

                srl_net_station_detail?.let {

                    val bean = res.toPOJO<NetStationDetailBean>().data
                    if (page == 1) {

                        tv_address_net_station_detail.text = "地址：${bean.address}"
                        tv_phone_net_station_detail.text = "电话：${bean.tel}"
                        tv_batter_num_net_station_detail.text = "电池数量：${bean.count}"

                        tv_call_net_station_detail.setOnClickListener {
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

                        rvAdapter.setNewData(bean.data)

                    } else {
                        rvAdapter.addData(bean.data)
                    }

                    page++


                }


                onFail { code, msg ->
                    EasyToast.DEFAULT.show(msg)
                }
            }
        }

    }


    private val rvAdapter by lazy { RvAdapter() }


    inner class RvAdapter :
        BaseQuickAdapter<NetStationDetailBean.Data.ItemData, BaseViewHolder>(R.layout.item_rv_net_station_detail) {
        override fun convert(
            helper: BaseViewHolder,
            item: NetStationDetailBean.Data.ItemData?
        ) {
            bothNotNull(helper, item) { a, b ->

                a.getView<TextView>(R.id.tv_battery_num)?.apply {
                    text = "编号：${b.device_id}"
                }


                a.getView<TextView>(R.id.tv_battery_deposit)
                    ?.apply {
                        text = "押金：¥${b.deposit}"
                    }


                a.getView<TextView>(R.id.tv_battery_model)?.apply {
                    text = "型号：${b.device_model}"
                }

                a.getView<TextView>(R.id.tv_battery_rent)?.apply {
                    text = "租金：¥${b.rent}/月"
                }
            }
        }
    }
}