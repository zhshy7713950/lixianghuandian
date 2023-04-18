package com.ruimeng.things.me.credit.bean

data class CreditContractListBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(

        //device_id int 设备编号
        //device_mode string 设备型号
        //loanperiod int 租赁周期 也叫贷款周期
        //status int 状态 0等待签约 1还款期 99已完成还款
        //next_day string 下一个还款日期
        //cur_period string 当前期数 3/8
        //contract_id string 合同ID/编号
        //business_id string 流水号 存在还款时需要的参数
        var business_id: String = "", // 40329b53-8981-4c1f-faf5-a1a115be9c10
        var contract_id: String = "", // 8edc8651-e869-f5fe-8fbe-cdb92006d738
        var cur_period: String = "", // 2/8
        var device_id: String = "", // 1499
        var device_mode: String = "", // 设备类型16
        var loanperiod: Int = 0, // 8
        var next_day: String = "", // 2019-01-15
        var status: Int = 0,// 1
        var show_firstpay: Int = 0,//是否显示首付按钮
        var credit_first_money: String = "" //首付money

    )
}