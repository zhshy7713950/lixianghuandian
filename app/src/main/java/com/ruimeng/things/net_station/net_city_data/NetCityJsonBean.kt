package com.ruimeng.things.net_station.net_city_data

import com.contrarywind.interfaces.IPickerViewData

data class NetCityJsonBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var child: List<Child> = listOf(),
        var id: String = "", // 130000
        var name: String = "", // 河北省
        var parent_id: String = "" // 0
    ) : IPickerViewData {
        override fun getPickerViewText(): String = name

        data class Child(
            var id: String = "", // 139000
            var name: String = "", // 省直辖县级行政区划
            var parent_id: String = "" // 130000
        ) : IPickerViewData {
            override fun getPickerViewText(): String = name
        }
    }
}