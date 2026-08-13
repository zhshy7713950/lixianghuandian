package com.ruimeng.things.net_station

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.App
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.net_station.bean.NetStationBean
import com.ruimeng.things.net_station.bean.filterSelf
import com.ruimeng.things.net_station.net_city_data.CityDataWorker
import com.ruimeng.things.net_station.net_city_data.NetCityJsonBean
import com.ruimeng.things.net_station.view.DefaultNetStationCtl
import com.ruimeng.things.net_station.view.NetStationView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.utils.unsafeLazy
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wongxd.base.MainTabFragment
import wongxd.common.bothNotNull
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http



class FgtNetStationItem : MainTabFragment() {

    private var contentView: View? = null
    private var cityRetryRunnable: Runnable? = null
    private var cityLoadingDialog: SweetAlertDialog? = null

    companion object {
        fun newInstance(): FgtNetStationItem {
            return FgtNetStationItem()
        }
    }
    override fun getLayoutRes(): Int = R.layout.fgt_net_station_item
    fun refresh(){
        val activeView = contentView ?: return
        activeView.findViewById<EditText>(R.id.et_search_station).text.clear()
        dealSelectCurrentCity(activeView)
    }
    class RefreshStationList
    @Subscribe
    public fun refreshStation(event: RefreshStationList) {
        contentView
            ?.findViewById<SmartRefreshLayout>(R.id.srl_station)
            ?.autoRefresh()
    }
    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        val activeView = mView ?: return
        contentView = activeView

        activeView.findViewById<SmartRefreshLayout>(R.id.srl_station).apply {
            setEnableLoadMore(false)
            setOnRefreshListener { getList(activeView) }
        }
        EventBus.getDefault().apply {
            if (!isRegistered(this@FgtNetStationItem)) {
                register(this@FgtNetStationItem)
            }
        }

        activeView.findViewById<RecyclerView>(R.id.rv_station).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = stationAdapter
        }

        CityDataWorker.initJsonData()

        activeView.findViewById<TextView>(R.id.tv_city).setOnClickListener {
            CityDataWorker.showOptionPicker(activity, "") { p, c ->
                provice = p
                city = c
                refreshCityPickerState(activeView)
            }
        }

        dealSelectCurrentCity(activeView)
        activeView.findViewById<TextView>(R.id.qfl_search_station).setOnClickListener {
            getList(activeView)
        }
    }

    override fun onDestroyView() {
        cancelPendingCityResolution()
        EventBus.getDefault().apply {
            if (isRegistered(this@FgtNetStationItem)) {
                unregister(this@FgtNetStationItem)
            }
        }
        contentView = null
        super.onDestroyView()
    }

    /**
     *  网点 服务站点和退还站点 默认定位选择到当前市
     */
    private fun dealSelectCurrentCity(requestView: View) {
        if (contentView !== requestView) return

        cancelPendingCityResolution()

        fun afterGetCityId(
            p: NetCityJsonBean.Data?,
            c: NetCityJsonBean.Data.Child?,
            dlg: SweetAlertDialog
        ) {
            if (contentView !== requestView) {
                dismissCityLoadingDialog(dlg, false)
                return
            }
            provice = p
            city = c
            refreshCityPickerState(requestView)
            getList(requestView)
            dismissCityLoadingDialog(dlg, true)
        }

        val (p, c) = CityDataWorker.getProvinceAndCityInfoByName(App.province, App.city)

        val dlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "获取城市数据中", true)
        cityLoadingDialog = dlg
        dlg.show()
        if (null == p) {
            lateinit var retryRunnable: Runnable
            retryRunnable = Runnable {
                if (cityRetryRunnable !== retryRunnable) return@Runnable
                cityRetryRunnable = null
                if (contentView !== requestView) {
                    dismissCityLoadingDialog(dlg, false)
                    return@Runnable
                }
                val (pp, cc) = CityDataWorker.getProvinceAndCityInfoByName(App.province, App.city)
                afterGetCityId(pp, cc, dlg)
            }
            cityRetryRunnable = retryRunnable
            requestView.postDelayed(retryRunnable, 2000L)
        } else {
            afterGetCityId(p, c, dlg)
        }
    }

    private fun cancelPendingCityResolution() {
        cityRetryRunnable?.let { runnable ->
            contentView?.removeCallbacks(runnable)
        }
        cityRetryRunnable = null

        cityLoadingDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        cityLoadingDialog = null
    }

    private fun dismissCityLoadingDialog(dialog: SweetAlertDialog, withAnimation: Boolean) {
        if (cityLoadingDialog === dialog) {
            cityLoadingDialog = null
        }
        if (!dialog.isShowing) return

        if (withAnimation) {
            dialog.dismissWithAnimation()
        } else {
            dialog.dismiss()
        }
    }


    private var provice: NetCityJsonBean.Data? = null
    private var city: NetCityJsonBean.Data.Child? = null
    private var oldCity: NetCityJsonBean.Data.Child? = null

    private fun refreshCityPickerState(requestView: View) {
        if (contentView !== requestView) return

        requestView.findViewById<TextView>(R.id.tv_city).text = city?.name
        if (oldCity != city) {
            oldCity = city
            getList(requestView)
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

    private fun getList(requestView: View) {
        if (contentView !== requestView) return

        val searchText = requestView
            .findViewById<EditText>(R.id.et_search_station)
            .text
            .toString()

        http {
            url = "apiv3/cgstationnetwork"
            params["city_id"] = city?.id ?: ""
            params["name"] = searchText
            params["deviceId"] = FgtHome.CURRENT_DEVICEID
            params["appType"] = "lxhd"

            onFinish {
                if (contentView === requestView) {
                    requestView
                        .findViewById<SmartRefreshLayout>(R.id.srl_station)
                        .finishRefresh()
                }
            }

            onSuccess { res ->
                if (contentView === requestView) {
                    val emptyView = requestView.findViewById<TextView>(R.id.tv_empty_net_station)
                    val stationCountView = requestView.findViewById<TextView>(R.id.tv_station_count)
                    data = res.toPOJO<NetStationBean>().data
                    if (data.isEmpty()){
                        emptyView.visibility = View.VISIBLE
                        stationAdapter.setNewData(null)
                        stationCountView.text = "已为您找到0个站点"
                    }else{
                        emptyView.visibility = View.GONE
                        currentIndex = 0
                        var list = data[currentIndex].filterSelf(FgtHome.getBatteryV()).list
                        // 将父级城市ID传递到每个站点项，供视图层使用
                        val parentCityId = data[currentIndex].city_id
                        list.forEach {
                            it.cityId = parentCityId
                            it.distance =AMapUtils.calculateLineDistance(LatLng(it.lat, it.lng), LatLng(App.lat, App.lng))
                            it.distanceStr =  if (it.distance >= 1000)
                                "${String.format("%.2f", (it.distance / 1000))}公里"
                            else
                                "${String.format("%.2f", it.distance)}米"
                        }
                        var list2  = list.sortedBy { it.distance }

                        stationAdapter.setNewData(list2)
                        stationCountView.text = "已为您找到${list2.size}个站点"
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
