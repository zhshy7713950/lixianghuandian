package com.ruimeng.things.net_station

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.net_station.bean.NetStationBean
import com.ruimeng.things.net_station.net_city_data.CityDataWorker
import com.ruimeng.things.net_station.net_city_data.NetCityJsonBean
import com.ruimeng.things.net_station.view.DefaultNetStationCtl
import com.ruimeng.things.net_station.view.NetStationView
import com.utils.unsafeLazy
import kotlinx.android.synthetic.main.fgt_net_station_item.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import wongxd.base.MainTabFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.bothNotNull
import wongxd.common.getCurrentAppAty
import wongxd.common.getSweetDialog
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.SystemUtils



class FgtNetStationItem : MainTabFragment() {

    companion object {
        fun newInstance(): FgtNetStationItem {
            return FgtNetStationItem()
        }
    }
    override fun getLayoutRes(): Int = R.layout.fgt_net_station_item
    fun refresh(){
        et_search_station?.text?.clear()
        dealSelectCurrentCity()
    }
    class RefreshStationList
    @Subscribe
    public fun refreshStation(event: RefreshStationList) {
        srl_station?.autoRefresh()
    }
    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        srl_station?.setEnableLoadMore(false)
        srl_station.setOnRefreshListener { getList() }
        EventBus.getDefault().register(this)

        rv_station.layoutManager = LinearLayoutManager(activity)
        rv_station.adapter = stationAdapter

        CityDataWorker.initJsonData()

        tv_city.setOnClickListener {
                        CityDataWorker.showOptionPicker(activity, "") { p, c ->
                provice = p
                city = c
                refreshCityPickerState()
            }
        }



        dealSelectCurrentCity()
        qfl_search_station.setOnClickListener {
            getList()
        }
    }

    /**
     *  网点 服务站点和退还站点 默认定位选择到当前市
     */
    private fun dealSelectCurrentCity() {

        fun afterGetCityId(
            p: NetCityJsonBean.Data?,
            c: NetCityJsonBean.Data.Child?,
            dlg: SweetAlertDialog?
        ) {
            provice = p
            city = c
            refreshCityPickerState()
            getList()
            dlg?.dismissWithAnimation()
        }

        val (p, c) = CityDataWorker.getProvinceAndCityInfoByName(App.province, App.city)

        val dlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "获取城市数据中", true)
        dlg.show()
        if (null == p) {
            val (pp, cc) = CityDataWorker.getProvinceAndCityInfoByName(App.province, App.city)
            doAsync {
                Thread.sleep(2000)
                uiThread {
                    tv_city?.let {
                        afterGetCityId(pp, cc, dlg)
                    }
                }
            }

        } else {
            afterGetCityId(p, c, dlg)
        }
    }


    private var provice: NetCityJsonBean.Data? = null
    private var city: NetCityJsonBean.Data.Child? = null
    private var oldCity: NetCityJsonBean.Data.Child? = null

    private fun refreshCityPickerState() {
        tv_city.text = city?.name
        if (oldCity != city) {
            oldCity = city
            getList()
        }
    }


    private var currentIndex = -1
    private val stationAdapter by lazy { StationRvAdapter() }

    private var data: List<NetStationBean.Data> = emptyList()

    public fun getStationList(): ArrayList<NetStationBean.Data.X> {
        var list = ArrayList<NetStationBean.Data.X>(stationAdapter.data.size)
        stationAdapter.data.toCollection(list)
        return list
    }

    private fun getList() {

        http {
            url = "apiv3/cgstationnetwork"
            params["city_id"] = city?.id ?: ""
            params["name"] = et_search_station.text.toString()
            params["appType"] = "lxhd"

            onFinish { srl_station?.finishRefresh() }

            onSuccess { res ->
                srl_station?.let {
                    data = res.toPOJO<NetStationBean>().data
                    if (data.isEmpty()){
                        tv_empty_net_station.visibility =View.VISIBLE
                        stationAdapter.setNewData(null)
                    }else{
                        tv_empty_net_station.visibility =View.GONE
                        currentIndex = 0
                        var list = data[currentIndex].list
                        list.forEach {
                            it.distance =AMapUtils.calculateLineDistance(LatLng(it.lat, it.lng), LatLng(App.lat, App.lng))
                            it.distanceStr =  if (it.distance >= 1000)
                                "${String.format("%.2f", (it.distance / 1000))}公里"
                            else
                                "${String.format("%.2f", it.distance)}米"
                        }
                        var list2  = list.sortedBy { it.distance }

                        stationAdapter.setNewData(list2)
                    }

                }
            }
        }
    }

    private val netStationCtl by unsafeLazy {
        DefaultNetStationCtl.create()
    }

    inner class StationRvAdapter :
        BaseQuickAdapter<NetStationBean.Data.X, BaseViewHolder>(R.layout.item_rv_station) {
        override fun convert(helper: BaseViewHolder, item: NetStationBean.Data.X?) {
            bothNotNull(helper, item) { a, b ->
                val netStationView = a.getView<NetStationView>(R.id.net_station_view)
                netStationView.setNewData(b)
                netStationView.bindCtl(netStationCtl)
            }
        }

    }


}