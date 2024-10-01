package com.ruimeng.things.home.bean

data class NewGetRentBean(
    val data: Data,
    val errcode: Int,
    val errmsg: String
) {
    data class Data(
        val paymentInfo : List<PaymentInfo>,
        val coupons: List<CouponsInfoBean>
    )
}