package   com.ruimeng.things.nearby.bean

data class LBSNearbyBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var address: String = "", // 这就是地址
        var lat: Double = 0.0, // 0.011
        var lng: Double = 0.0, // 0.022
        var mobile: String = "", // 1542875656
        var nickname: String = "", // 商户1542875656
        var notice: String = "" // 这是公告要支持换行
    )
}