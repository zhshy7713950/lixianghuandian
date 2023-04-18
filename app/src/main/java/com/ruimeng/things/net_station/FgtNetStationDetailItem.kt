package com.ruimeng.things.net_station

import android.os.Bundle
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationDetailBeanTwo
import com.utils.LogHelper
import wongxd.base.BaseBackFragment


class FgtNetStationDetailItem : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_net_station_detail_item

    companion object {
        fun newInstance(data: NetStationDetailBeanTwo.Data.ExchangeBean): FgtNetStationDetail {
            return FgtNetStationDetail().apply {
                arguments = Bundle().apply {
                    putSerializable("data", data.toString())
                }
            }
        }
    }

    private val getData by lazy { arguments?.getSerializable("data") as NetStationDetailBeanTwo.Data.ExchangeBean }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        val dataList=getData.device
        LogHelper.i("data===","===dataList.size===${dataList.size}")
    }
}