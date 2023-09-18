package com.ruimeng.things.me.contract.bean

import com.ruimeng.things.home.bean.PaymentOption

data class MyContractDetailBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var contract_id: String = "", // bf68081f-0ead-ddbf-57ae-89093105ab14
        var deposit: String = "",
        var device_id: Int = 0, // 968
        var model_str: String = "", // 型号a68
        var rent: String = "",
        var renttime_str: String = "", // 5 个月
        var sign_pngs: List<SignPng> = listOf(),
        var pdf: String = "",
        var paymentName: String = "",
        var begin_time: String = "",
        var exp_time: String = "",
        var down_sign: Int = 0,
        val userOptions :List<PaymentOption>? = null
    ) {
        data class SignPng(
            var png: String = "" // https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1577341978542&di=4c5c522ef9cfa79cf718fcf6ef3bd243&imgtype=0&src=http%3A%2F%2Fm.360buyimg.com%2Fn12%2Fjfs%2Ft2194%2F6%2F2805788145%2F52875%2Fa40fa579%2F57174d1aN66ce14d9.jpg%2521q70.jpg
        )
    }
}