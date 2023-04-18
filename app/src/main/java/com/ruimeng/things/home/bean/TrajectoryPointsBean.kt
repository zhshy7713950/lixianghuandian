package com.ruimeng.things.home.bean

data class TrajectoryPointsBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var lbs: String = "" // 104.062343478733,30.540101725261
    )
}