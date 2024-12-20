package com.ruimeng.things.net_station.bean

data class GetCityInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var province: String = "",
        var city: String = "",
        var area: String = "",
        var city_id: String = ""
    )
}
