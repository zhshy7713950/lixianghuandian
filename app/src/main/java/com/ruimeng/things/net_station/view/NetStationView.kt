package com.ruimeng.things.net_station.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean

class NetStationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
), INetStationView {
    private lateinit var ctl: INetStationController

    init {
        LayoutInflater.from(context).inflate(R.layout.view_net_station_item, this, true)
    }

    override fun setNewData(data: NetStationBean.Data.X) {

    }

    override fun bindCtl(ctl: INetStationController) {
        this.ctl = ctl
    }

}