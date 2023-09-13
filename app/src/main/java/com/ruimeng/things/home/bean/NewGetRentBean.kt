package com.ruimeng.things.home.bean

data class NewGetRentBean(
    val data: List<PaymentInfo>,
    val errcode: Int,
    val errmsg: String
) {

}