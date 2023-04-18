package   com.ruimeng.things.shop.bean

data class TkConfigBean(
        var `data`: Data = Data(),
        var errcode: Int = 0, // 200
        var errmsg: String = "", // 操作成功
        var msg: String = "" // 操作成功
) {
    data class Data(
            var app_config: AppConfig = AppConfig(),
            var corner: Corner = Corner(),
            var token: String = "" // 06cab4df-f945-b00a-1df4-8223221afab5
    ) {
        data class AppConfig(
                var app_name: String = "", // 赚乐购
                var btn_label: String = "", // 立即兑换
                var buy_fixed: String = "", // 1.00
                var buy_mode: String = "", // 0
                var buy_proportion: String = "", // 1.00
                var detail_remark: String = "",
                var hot_search: String = "", // 女装,男装,电器
                var id: String = "", // 587
                var led: String = "", // 全网淘宝优惠券,购物立减,赶快使用话费兑换!
                var pid: String = "", // mm_127018821_44550947_15113450204
                var reappearance_open: String = "", // 0
                var service_phone: String = "", // 112
                var service_qq: String = "", // 112
                var service_wx: String = "", // 112
                var user_id: String = "", // 66
                var video_show: String = "" // 0
        )

        data class Corner(
                var item_type: List<ItemType> = listOf()
        ) {
            data class ItemType(
                    var id: Int = 0, // 3
                    var rgb: String = "", // FF4500
                    var txt: String = "" // 拼多多
            )
        }
    }
}