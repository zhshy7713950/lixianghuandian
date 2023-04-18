package com.ruimeng.things.me.contract.bean

data class ContractSignStepOneBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var contract_id: String = "", // dcf28bf7-1edb-84c8-5487-1eaf6bfa3bb0
        var deposit: String = "", // 2161
        var device_id: Int = 0, // 858
        var model_str: String = "", // 型号a55
        var rent: String = "", // 970
        var renttime_str: String = "", // 5 个月
        var sign_pngs: List<SignPng> = listOf(),
        var wait_sec: Int = 0 // 3
    ) {
        data class SignPng(
            var png: String = "" // https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1577341978542&di=4c5c522ef9cfa79cf718fcf6ef3bd243&imgtype=0&src=http%3A%2F%2Fm.360buyimg.com%2Fn12%2Fjfs%2Ft2194%2F6%2F2805788145%2F52875%2Fa40fa579%2F57174d1aN66ce14d9.jpg%2521q70.jpg
        )
    }
}