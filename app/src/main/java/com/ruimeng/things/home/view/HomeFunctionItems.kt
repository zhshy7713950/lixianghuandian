package com.ruimeng.things.home.view

import java.net.URLEncoder

enum class HomeFunctionAction {
    SELF_SERVICE_OPEN,
    FREEZE_OR_UNFREEZE,
    EXTRA_RENT,
    CUSTOMER_SERVICE,
    RECOMMEND_GIFT,
    SWITCH_BATTERY,
    NEARBY_STATIONS,
    MY_CONTRACT,
    CUSTOMER_HOTLINE,
    BATTERY_LOCATION,
    MY_COUPONS,
    LOW_BATTERY_REMINDER,
    LIXIANG_MINI_PROGRAM,
    CHANGE_MOBILE,
    TERMINATE_RENT
}

internal enum class HomeFunctionControl {
    MORE,
    COLLAPSE
}

internal data class HomeFunctionItem(
    val title: String,
    val action: HomeFunctionAction? = null,
    val control: HomeFunctionControl? = null
) {
    init {
        require((action == null) != (control == null))
    }

    val iconName: String
        get() = title
}

internal class HomeFunctionZoneState {
    var expanded: Boolean = false
        private set

    var packageFrozen: Boolean = false
        private set

    fun setPackageFrozen(frozen: Boolean) {
        packageFrozen = frozen
    }

    fun toggleExpanded() {
        expanded = !expanded
    }

    fun items(): List<HomeFunctionItem> = buildHomeFunctionItems(expanded, packageFrozen)
}

internal fun buildHomeFunctionItems(
    expanded: Boolean,
    packageFrozen: Boolean
): List<HomeFunctionItem> {
    val items = mutableListOf(
        HomeFunctionItem("电池被吞", HomeFunctionAction.SELF_SERVICE_OPEN),
        HomeFunctionItem(
            if (packageFrozen) "解冻套餐" else "冻结套餐",
            HomeFunctionAction.FREEZE_OR_UNFREEZE
        ),
        HomeFunctionItem("额外租电", HomeFunctionAction.EXTRA_RENT),
        HomeFunctionItem("客服中心", HomeFunctionAction.CUSTOMER_SERVICE),
        HomeFunctionItem("推荐有礼", HomeFunctionAction.RECOMMEND_GIFT),
        HomeFunctionItem("切换电池", HomeFunctionAction.SWITCH_BATTERY),
        HomeFunctionItem("附近点位", HomeFunctionAction.NEARBY_STATIONS),
        HomeFunctionItem("我的合约", HomeFunctionAction.MY_CONTRACT),
        HomeFunctionItem("客服热线", HomeFunctionAction.CUSTOMER_HOTLINE)
    )

    if (expanded) {
        items += listOf(
            HomeFunctionItem("电池定位", HomeFunctionAction.BATTERY_LOCATION),
            HomeFunctionItem("我的优惠券", HomeFunctionAction.MY_COUPONS),
            HomeFunctionItem("低电提醒", HomeFunctionAction.LOW_BATTERY_REMINDER),
            HomeFunctionItem("锂享小程序", HomeFunctionAction.LIXIANG_MINI_PROGRAM),
            HomeFunctionItem("变更手机号", HomeFunctionAction.CHANGE_MOBILE),
            HomeFunctionItem("退租", HomeFunctionAction.TERMINATE_RENT),
            HomeFunctionItem("收起", control = HomeFunctionControl.COLLAPSE)
        )
    } else {
        items += HomeFunctionItem("更多", control = HomeFunctionControl.MORE)
    }
    return items
}

internal fun homeFunctionIconUrl(iconName: String): String {
    val encodedFileName = URLEncoder.encode("首页-$iconName.png", "UTF-8")
    return "https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/$encodedFileName"
}
