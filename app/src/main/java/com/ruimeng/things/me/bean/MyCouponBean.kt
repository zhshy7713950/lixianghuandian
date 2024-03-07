package com.ruimeng.things.me.bean

import com.contrarywind.interfaces.IPickerViewData

data class MyCouponBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 200, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var coupon_id: String = "", // 1542167707
        var coupon_label: String = "", // 100优惠卷2
        var coupon_limit: String = "", // 10
        var coupon_price: String = "", // 100
        var exp_time: String = "", // 1542167707
        var is_use: String = "" ,// 1542167707
        var limit_day: String = "" ,// 1542167707
    var id : Int = 0,
        var expond :Boolean = false
    ) : IPickerViewData {
        override fun getPickerViewText(): String {
            return coupon_label
        }
    }
}