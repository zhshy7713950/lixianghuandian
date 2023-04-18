package com.ruimeng.things.home.bean

data class ChangeRentBatteryBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var device_info: DeviceInfo = DeviceInfo(),
        var diff_pay: DiffPay = DiffPay(),
        var new_device_info: NewDeviceInfo = NewDeviceInfo(),
        var tradno: String = "" // d32e0eb6-0438-a952-7785-96a54a4c9008
    ) {
        data class DeviceInfo(
            var deposit: String = "", // 100.23
            var device_id: Int = 0, // 19
            var model_str: String = "", // 型号32
            var rent: String = "", // 200.95
            var rent_time: String = "" // 1个月
        )

        data class DiffPay(
            var deposit: String = "", // 22.44
            var rent: String = "", // 11.93
            var total_diff: String = "" // 188.00
        )

        data class NewDeviceInfo(
            var deposit: String = "", // 22.44
            var device_id: Int = 0, // 160
            var model_str: String = "", // 型号53
            var rent: String = "", // 11.93
            var rent_time: String = "" // 5个月
        )
    }
}