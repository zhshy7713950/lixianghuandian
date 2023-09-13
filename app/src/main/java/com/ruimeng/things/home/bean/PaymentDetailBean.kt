package com.ruimeng.things.home.bean

data class PaymentDetailBean(
    var `data`: Data ,
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {


data class Data(
    val agent_code: String,
    val agent_id: String,
    val begin_time: String,
    val contract_cg_mode: String,
    val contract_id: String,
    val contract_mode: String,
    val credit_first_money: Int,
    val credit_firstpay: String,
    val deposit: Double,
    val deposit_option_id: String,
    val deposit_status: String,
    val device_id: String,
    val exp_time: String,
    val ext_code_a: String,
    val ext_code_b: String,
    val id: String,
    val insurance: String,
    val is_credit: String,
    val is_default: String,
    val is_host: String,
    val is_lock: Any,
    val is_sign: String,
    val is_test: String,
    val lock_remark: Any,
    val model_id: String,
    val new_payment: String,
    val old_endtime: String,
    val pay_type: String,
    val paymentInfo: PaymentInfo,
    val payment_id: String,
    val remark: String,
    val rent_day: String,
    val rent_money: Double,
    val rent_status: String,
    val rent_time: String,
    val total_rent_money: Double,
    val user_id: String
)


}