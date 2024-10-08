package com.ruimeng.things.net_station.view

import android.content.Context
import com.ruimeng.things.net_station.bean.NetStationBean

class DefaultNetStationCtl: INetStationController {

    companion object{
        fun create(): INetStationController {
            return DefaultNetStationCtl()
        }
    }

    override fun onItemClick(data: NetStationBean.Data.X, context: Context) {
    }

    override fun onTelClick(data: NetStationBean.Data.X, context: Context) {
    }

    override fun onNavigationClick(data: NetStationBean.Data.X, context: Context) {
    }

    override fun bindView(view: INetStationView) {
    }
}