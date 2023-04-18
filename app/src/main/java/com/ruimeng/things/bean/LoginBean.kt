package com.ruimeng.things.bean

data class LoginBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var token: String = "", // ec05681e-d9e2-19a9-1881-59f693435a8d
        var userinfo: UserInfoBean.Data.UserInfo = UserInfoBean.Data.UserInfo()
    )


}