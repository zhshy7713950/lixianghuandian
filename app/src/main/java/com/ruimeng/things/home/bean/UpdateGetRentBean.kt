package com.ruimeng.things.home.bean

data class UpdateGetRentBean(
    val data: Data,
    val errcode: Int,
    val errmsg: String
) {
 data class Data(
     val paymentInfo : List<PaymentInfo>,
     val baseInfo :PaymentInfo,
     val userOptions : List<PaymentOption>,
     val options : List<PaymentOption>,
     val coupons: List<CouponsInfoBean>
 )
}