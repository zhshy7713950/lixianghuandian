package com.ruimeng.things.home.bean

data class GetRentBean(
    var `data`: Data = Data(),
    var errcode: Int = 200, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var btn_return: Int = 0,
        var contract_id: String = "",
        var deposit_option_label: String = "",
        var device_info: DeviceInfo = DeviceInfo(),
        var payment: List<Payment> = arrayListOf(),
        var pay_type: List<PayType> = listOf()
    ) {
        data class Payment(
            var id: String = "", // 2
            var agent_id: String = "",
            var is_show: String = "", // 1
            var model_id: String = "", // 1
            var month: String = "", // 1
            var name: String = "", // 支付2
            var price: String = "", // 200.00
            var price_host: String = "", // 300.00
            var remark: String = "", // 支付2
            var weight: String = "",// 1
            var insurance_price: String = "",
            var insurance_price_host: String = ""
        )

        data class DeviceInfo(
            var device_id: Int =0, // 1025
            var device_model: String = "", // AAAAA
            var code: String = "",
            var is_host: String = ""
        )

        data class PayType(
            var channel: String = "", // cash
            var is_show: Int = 0 // 0
        )
    }
}