package com.ruimeng.things.home.bean

data class ChangeDepositCombinationBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var contract_id: String = "", // 05a39300-2321-da88-c582-002250c89b05
        var cur_deposit: String = "",
        var cur_deposit_name: String = "", // 押金100，保费20
        var deposit_option: List<DepositOption> = listOf(),
        var device_id: Int = 0, // 1025
        var tips: String = "" // 这是提示文字
    ) {
        data class DepositOption(
            var deposit: String = "",
            var deposit_host: String = "",
            var id: Int = 0, // 3
            var name: String = "" // 押金2000，保费包年
        )
    }
}