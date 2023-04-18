package com.ruimeng.things.home.bean

import com.contrarywind.interfaces.IPickerViewData
import com.google.gson.annotations.SerializedName


data class GetDepositBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var code: String = "", // 123456
        var deposit: String = "",
        var deposit_host: String = "",
        var deposit_option: List<DepositOption> = listOf(),
        var device: Device = Device(),
        @SerializedName("package")
        var packageList: List<Package> = listOf(),
        var pay_type: List<PayType> = listOf()
    ) {
        data class DepositOption(
            var deposit: String = "",
            var deposit_host: String = "",
            var id: Int = 0, // 3
            var name: String = "" // 押金2000，保费包年
        ) : IPickerViewData {
            override fun getPickerViewText(): String = name

        }

        data class Device(
            var device_id: Int = 0, // 1024
            var device_model: String = "" // 60伏40安
        )

        data class Package(
            var deposit: String = "", // 1000
            var deposit_host: String = "", // 1000
            var id: String = "", // 106
            var name: String = "", // 租一个月6040套餐
            var deposit_option: List<DepositOption> = listOf()
        )

        data class PayType(
            var channel: String = "", // third_credit
            var id: Int = 0, // 4
            var is_show: Int = 0 // 1
        )
    }
}
