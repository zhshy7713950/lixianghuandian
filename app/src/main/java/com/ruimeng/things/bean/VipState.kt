package com.ruimeng.things.bean

import android.graphics.Color

class VipState(
    val startDay: Int,
    val endDay: Int,
    val stateCN: String,
    val stateEN: String,
    val stateColor: Int,
    val descriptionColor: Int,
    val isKing: Boolean
)

fun myVipLevel(onlineTime: Int) = vipStates.find { onlineTime >= it.startDay && onlineTime < it.endDay }

fun VipState.bgImage() =
    "https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/level-${this.stateEN}-bg@3x.png"

fun VipState.stateImage() =
    "https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/level-${this.stateEN}-icon@3x.png"

fun VipState.description(onlineTime: Int): String {
    return if (isKing) {
        "在网${onlineTime}天"
    } else {
        val nextState = vipStates[vipStates.indexOf(this) + 1]
        "在网${onlineTime}天，还需${nextState.startDay - onlineTime}天可升级至${nextState.stateCN}"
    }
}

val vipStates = listOf(
    VipState(
        0,
        180,
        "青铜会员",
        "bronze",
        Color.parseColor("#785837"),
        Color.parseColor("#3E3E3E"),
        false
    ),
    VipState(
        181,
        360,
        "白银会员",
        "silver",
        Color.parseColor("#46575F"),
        Color.parseColor("#3E3E3E"),
        false
    ),
    VipState(
        361,
        540,
        "黄金会员",
        "gold",
        Color.parseColor("#9F8001"),
        Color.parseColor("#3E3E3E"),
        false
    ),
    VipState(
        541,
        720,
        "铂金会员",
        "platinum",
        Color.parseColor("#74787B"),
        Color.parseColor("#3E3E3E"),
        false
    ),
    VipState(
        721,
        1080,
        "钻石会员",
        "diamond",
        Color.parseColor("#6D6D6C"),
        Color.parseColor("#3E3E3E"),
        false
    ),
    VipState(
        1081,
        Int.MAX_VALUE,
        "最强王者",
        "goat",
        Color.parseColor("#FFFFFF"),
        Color.parseColor("#FFFFFF"),
        true
    )
)