package com.ruimeng.things.home.bean

data class DeviceDetailBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var credit: DeviceCredit = DeviceCredit(),
        var device_base: DeviceBase = DeviceBase(),
        var device_contract: DeviceContract = DeviceContract(),
        var popmsg: PopMsgBean = PopMsgBean(),
        var device_id: Int = 0 // 1025
    ) {
        data class DeviceContract(
            var agent_id: String = "", // 0
            var begin_time: String = "", // 1542165966
            var contract_id: String = "", // 312d0f84-626c-a171-9f1a-310b6e98eb2a
            var deposit: String = "", // 100
            var deposit_status: String = "", // 1
            var device_id: String = "", // 1025
            var exp_time: String = "", // 1942252366
            var id: String = "", // 1
            var is_default: String = "", // 1
            var is_host: String = "", // 1
            var remark: String = "", // wongxd
            var rent_day: String = "", // 1
            var rent_money: String = "", // 100
            var rent_status: String = "", // 0
            var rent_time: String = "", // 1542165966
            var total_rent_money: String = "", // 100
            var user_id: String = "", // 2
            var contract_mode: Int = 1 ,//合约类型，1正常合约，2可绑定其他设备
            var is_sign: String = "",
            var contract_cg_mode: String = ""
        )

        data class DeviceBase(
            var use_mileage: String = "",
            var mileage: String = "",
            var speed_avg: String = "",
            var alert_msg: String = "",
            var alert_status: Int = 0, // 0
            var device_id: String = "", // 1025
            var device_status: String = "", // 1
            var devicenum: String = "", // 17
            var diy1: String = "", // 0
            var electric: String = "", // 65299
            var equilibrium: String = "", // 0
            var equilibriumtop: String = "", // 0
            var fet: String = "", // 3
            var is_online: Int = 0, // 1
            var last_status: String = "", // 2
            var last_time: String = "", // 1542249382
            var lastcapacity: String = "", // 2622
            var loopnum: String = "", // 2
            var nntc: String = "", // 2874
            var ntc: String = "", // 2
            var productiondate: String = "", // 9554
            var protect: String = "", // 0
            var rsoc: String = "", // 63
            var signallength: String = "", // 14
            var silenttime: String = "", // 65535
            var softversion: String = "", // 34
            var standardcapacity: String = "", // 4160
            var temperature: String = "", // 14.30
            var totalvoltage: String = "" // 6616
        )

        //        is_credit int 是否信用支付合约 1是0否 如果是 在主页列表新增显示 去还款 带上contract_number参数 请求 /api/repaymentlist
//        contract_id string 信用支付合同
//        credit_firstpay int 是否完成首付
//        credit_first_money decimal 首付金额
        data class DeviceCredit(
            var is_credit: Int = 0,
            var contract_id: String = "",
            var credit_firstpay: Int = 0,
            var credit_first_money: String = ""
        )

        data class PopMsgBean(
            var show_msg: Int = 0,
            var msg: String = ""
        )
    }
}