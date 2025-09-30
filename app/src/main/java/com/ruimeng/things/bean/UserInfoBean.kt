package com.ruimeng.things.bean

import org.json.JSONObject

data class UserInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var token: String = "", // a5064db6-c7ab-3319-6a56-5e7697ed2580
        var userinfo: UserInfo = UserInfo()
    ) {
        data class UserInfo(
            var mp_follow: Int = 0,
            var agent_id: String = "", // 0
            var gh_openid: String = "",
            var id: String = "", // 2
            var is_lock: String = "", // 0
            var logo: String = "", // http://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83epndvDUckfxN4zib100XbIOnSsvLCGXBKD695BK49u2msibk5GZZDHVQbKUHZxpPNwPtmPOYspK28Zg/132
            var mobile: String = "", // 18190697548
            var mobile_bind: String = "", // 1
            var nickname: String = "", // 5bCY56uL
            var password: String = "",
            var reg_time: String = "", // 1542108211
            var salt: String = "",
            var unionid: String = "", // oN-6v0R9CHat3k_8GQciAzKZDRoU
            var username: String = "", // ocH-R0cBhrPGeM3phUlIxlgZWzqY
            var wxapp_openid: String = "", // ocH-R0cBhrPGeM3phUlIxlgZWzqY
            var devicenumber: String = "",
            var devicedeposit: String = "",
            var realname_auth: Int = 0,
            var is_debug: Int = 0,  //1是调试账号 0正常账号
            var freeMark: String? = null, //1 存在免押，0不存在免押金
            var phone: String = "",
            var online_time: Int = 0,
            var city: String = "",
            var electric: Any? = null,
            var isBindAccount: Int = 0, // 1-已绑定，0-未绑定
            var isVoiceActived: Int = 0, // 1-启用语音提示，0-关闭语音提示
        ){
            fun getElectric(): Electric?{
                return getElectricList().firstOrNull()
            }

            private fun getElectricList(): List<Electric> {
                return when (electric) {
                    is List<*> -> {
                        // 处理数组情况
                        (electric as List<Map<String, Any>>).mapNotNull { mapToElectricItem(it) }
                    }
                    is Map<*, *> -> {
                        // 处理对象情况
                        listOfNotNull(mapToElectricItem(electric as Map<String, Any>))
                    }
                    else -> emptyList()
                }
            }

            private fun mapToElectricItem(map: Map<String, Any>): Electric? {
                return try {
                    Electric(
                        exchangeTimes = map["exchangeTimes"]?.toString()?:"",
                        useElectr = map["useElectr"]?.toString()?:"",
                        cityName = map["cityName"]?.toString()?:"",
                        days = map["days"]?.toString()?:"",
                        perDayElectric = map["perDayElectric"]?.toString()?:""
                    )
                } catch (e: Exception) {
                    null
                }
            }

        }

        data class Electric(
            var exchangeTimes: String = "",
            var useElectr: String = "",
            var cityName: String = "",
            var days: String = "",
            var perDayElectric: String = "",
        )


    }
}

fun UserInfoBean.Data.UserInfo.showName(): String{
    return if(0 == realname_auth){
        "未实名$mobile"
    }else{
        nickname
    }
}