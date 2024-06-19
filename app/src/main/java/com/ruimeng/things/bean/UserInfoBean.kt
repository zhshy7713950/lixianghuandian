package com.ruimeng.things.bean

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
            var freeMark: String? = null //1 存在免押，0不存在免押金
        )
    }
}