package com.ruimeng.things.net_station.view

import android.content.Context
import com.base.mvc.IBaseController
import com.base.mvc.IBaseView
import com.ruimeng.things.net_station.bean.NetStationBean

interface INetStationView: IBaseView<INetStationController> {
    fun setNewData(data: NetStationBean.Data.X)
}

interface INetStationController: IBaseController<INetStationView> {
    fun onItemClick(data: NetStationBean.Data.X,context: Context)
    fun onTelClick(data: NetStationBean.Data.X,context: Context)
    fun onNavigationClick(data: NetStationBean.Data.X,context: Context)
}