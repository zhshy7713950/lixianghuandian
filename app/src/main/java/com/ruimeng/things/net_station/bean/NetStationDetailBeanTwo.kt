package com.ruimeng.things.net_station.bean

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
                var lockStatus: Int = -1,
            )
        }
    }
}