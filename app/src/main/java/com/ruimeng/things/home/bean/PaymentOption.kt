package com.ruimeng.things.home.bean

class PaymentOption(
    val active_time: String ="",
    var change_times: String="",
    val id: String ="",
    var name: String ="",
    val option_id: String="",
    val option_type: String="",
    val package_id: String="",
    var show_start_time: String ="",
    val start_time: String ="",
    val end_time: String ="",
    var show_end_time: String ="",
    var active_status: String ="",
    val price: String="",
    var single_option :Boolean = false //是否是单次换电
) {
}