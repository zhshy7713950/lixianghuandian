package com.ruimeng.things.me.credit.bean

import com.google.gson.annotations.SerializedName

data class CreditReckoningInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var base: Base = Base(),
        var list: List<X> = listOf()
    ) {
        data class X(
            @SerializedName("cur_period")
            var curPeriod: String = "", // 3/8
            var business_id:String = "",
            var day: String = "", // 2019-01-03
            var loanamount: String = "", // 238
            var status: Int = 0// 2
        )

        data class Base(
            @SerializedName("contract_id")
            var contractId: String = "", // 5d3ccb15-12e2-2709-dad4-1fa7e57ac2ae
            @SerializedName("cur_period")
            var curPeriod: String = "", // 3/8
            @SerializedName("device_id")
            var deviceId: String = "", // 437
            @SerializedName("device_mode")
            var deviceMode: String = "", // 型号A
            var loanperiod: Int = 0, // 8
            @SerializedName("next_day")
            var nextDay: String = "", // 2019-01-09
            var status: Int = 0 // 1
        )
    }
}