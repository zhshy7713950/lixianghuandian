package com.ruimeng.things.home.bean

data class BannerData(
    var `data`: List<BannerInfo> = mutableListOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = ""
)

data class BannerInfo(
    val imgSrc: String,
    val title: String,
    val opType: Int,
    val linkUrl: String
) 