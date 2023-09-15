import com.ruimeng.things.home.bean.PaymentOption

data class MyContractListBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var begin_time: String = "", // 2019-11-2
        var btn_return: Int = 0, // 1
        var btn_sign: Int = 0, // 0
        var contract_id: String = "", // ebef4f78-26e1-fb51-b728-8f32ae1d66ca
        var device_id: Int = 0, // 499
        var end_time: String = "", // 2019-12-3
        var model_str: String = "", // 型号a70
        var renttime_str: String = "", // 8 个月
    var userOptions : List<PaymentOption>
    )
}