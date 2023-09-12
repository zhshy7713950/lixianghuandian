package com.ruimeng.things.bean

data class BaseResultBean<T>(
    var `data`: T ,
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {

}