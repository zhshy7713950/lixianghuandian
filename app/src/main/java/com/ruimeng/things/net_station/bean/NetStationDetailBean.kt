package com.ruimeng.things.net_station.bean

data class NetStationDetailBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var address: String = "", // 贵州省六盘水钟山区花园路1号106室-1号
        var city: String = "", // 六盘水市
        var city_id: String = "", // 520200
        var count: String = "", // 12
        var `data`: List<ItemData> = listOf(),
        var id: String = "", // 78
        var lat: Double = 0.0, // 26.575611
        var lng: Double = 0.0, // 104.841897
        var province_id: String = "", // 520000
        var site_name: String = "", // 享锂来贵州六盘水市代理点
        var tag: String = "", // 贵州六盘水市代理点
        var tel: String = "" // 0858-8966338
    ) {
        data class ItemData(
            var device_id: String = "",
            var deposit: String = "", // 600
            var device_model: String = "", // 72伏45安
            var rent: String = "" // 258
        )
    }
}