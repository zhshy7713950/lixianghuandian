package com.ruimeng.things.net_station.view

import android.content.Context
import com.base.mvc.IBaseController
import com.base.mvc.IBaseView
import com.ruimeng.things.net_station.bean.NetStationBean
import com.ruimeng.things.net_station.bean.NetStationDetailBeanTwo

interface INetStationView: IBaseView<INetStationController> {
    fun setNewData(data: NetStationBean.Data.X)
}

interface INetStationController: IBaseController<INetStationView> {
    fun onItemClick(data: NetStationBean.Data.X,context: Context)
    fun onTelClick(data: NetStationBean.Data.X,context: Context)
    fun onNavigationClick(data: NetStationBean.Data.X,context: Context)
}

interface INetStationDetailView: IBaseView<INetStationDetailController> {
    fun setNewData(data: NetStationDetailBeanTwo.Data)
}

interface INetStationDetailController: IBaseController<INetStationDetailView> {
    fun onUpdateClick()
    fun onTelClick(data: NetStationDetailBeanTwo.Data,context: Context)
    fun onNavigationClick(data: NetStationDetailBeanTwo.Data,context: Context)
}