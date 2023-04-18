package com.ruimeng.things.net_station.bean


data class NetWorkShowBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var cg_service_show: Int = 0,// 是否显示换电服务站点 1是0否
        var cg_rent_show: Int = 0,// 是否显示租电服务站点 1是0否
        var cg_show: Int = 0// 是否显示换电站点 1是0否
    )
}