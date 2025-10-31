package com.ruimeng.things.net_station.bean

import kotlinx.android.parcel.RawValue

data class NetStationDetailBeanTwo(
    var `data`: Data = Data(),
    var errcode: Int = 0,
    var errmsg: String = ""
) {
    data class Data(
        var address: String = "",
        var device_num: String = "",
        var device_available_num: String = "",
        var exchange: List<ExchangeBean> = listOf(),
        var recommend_str: String = "",
        var recommend_id: String = "",
        var recommend_pos: String = "",
        var code: String = "",
        var tel: String = "",
        var site_name: String = "",
        var lng: Double = 0.0,
        var lat: Double = 0.0,
        var isOnline: Int = 0,
        var cellNum: String = "",
        var site_image: List<String> = listOf(),
        var telData:List<NetStationBean.Data.TelData> = listOf(),
        var available_arr: NetStationBean.Data.Model = NetStationBean.Data.Model(),
        var workTime: String = "",
        var swCabSocControl: String = "",
        // 新增：不同伏数与安数的可换数统计
        var batTypeCount: @RawValue HashMap<String, HashMap<String, Int>> = hashMapOf(),
        // 新增：所属城市ID（由父级 Data.city_id 传入）
        var city_id: String = "",
    ) {
        data class ExchangeBean(
            var name: String = "",
            var id: String = "",
            var device: List<DeviceBean> = listOf()
        ) {
            data class DeviceBean(
                var id: String = "",
                var device_id: String = "",
                var electricity: String = "",
                var status: Int = -1,
                var pos: String = "",
                var device_type: String = "",
                // 电池安时，例如："50安"、"80安"，用于泸州城市标签显示
                var device_ah: String = "",
                var lockStatus: Int = -1,
            )
        }
    }
}