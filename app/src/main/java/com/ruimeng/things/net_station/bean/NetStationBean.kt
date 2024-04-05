package com.ruimeng.things.net_station.bean

data class NetStationBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var city: String = "", // 上海市
        var city_id: Int = 0, // 2
        var list: List<X> = listOf()
    ) {
        data class X(
            var address: String = "", // 成都市高新区天府五街6号
            var lat: Double = 0.0, // 30.542695
            var lng: Double = 0.0, // 104.059652
            var id: String = "",
            var site_name: String = "", // 服务中心4
            var tag: String = "",
            var count: String = "",
            var tel: String = "", // 028-85214458
        var distance:Float = 0.0f,
            var distanceStr :String =""
        )
    }
}