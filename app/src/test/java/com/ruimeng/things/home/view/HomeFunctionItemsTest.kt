package com.ruimeng.things.home.view

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeFunctionItemsTest {

    @Test
    fun collapsedItemsMatchTwoRowOrder() {
        val items = buildHomeFunctionItems(expanded = false, packageFrozen = false)

        assertEquals(
            listOf("电池被吞", "冻结套餐", "额外租电", "客服中心", "推荐有礼", "切换电池", "附近点位", "我的合约", "客服热线", "更多"),
            items.map { it.title }
        )
        assertEquals(HomeFunctionControl.MORE, items.last().control)
        assertNull(items.last().action)
    }

    @Test
    fun expandedItemsMatchFourRowOrder() {
        val items = buildHomeFunctionItems(expanded = true, packageFrozen = false)

        assertEquals(
            listOf(
                "电池被吞", "冻结套餐", "额外租电", "客服中心", "推荐有礼",
                "切换电池", "附近点位", "我的合约", "客服热线", "电池定位",
                "我的优惠券", "低电提醒", "锂享小程序", "变更手机号", "退租", "收起"
            ),
            items.map { it.title }
        )
        assertEquals(HomeFunctionAction.BATTERY_LOCATION, items[9].action)
        assertEquals(HomeFunctionControl.COLLAPSE, items[15].control)
        assertEquals(15, items.mapNotNull { it.action }.distinct().size)
    }

    @Test
    fun firstNineBusinessActionsStayStableAcrossExpansion() {
        val collapsed = buildHomeFunctionItems(expanded = false, packageFrozen = false)
        val expanded = buildHomeFunctionItems(expanded = true, packageFrozen = false)

        assertEquals(
            collapsed.take(9).map { it.action },
            expanded.take(9).map { it.action }
        )
    }

    @Test
    fun frozenStateOnlyChangesSecondItemPresentation() {
        val frozen = buildHomeFunctionItems(expanded = false, packageFrozen = true)[1]
        val unfrozen = buildHomeFunctionItems(expanded = false, packageFrozen = false)[1]

        assertEquals("解冻套餐", frozen.title)
        assertEquals("解冻套餐", frozen.iconName)
        assertEquals("冻结套餐", unfrozen.title)
        assertEquals(frozen.action, unfrozen.action)
        assertEquals(HomeFunctionAction.FREEZE_OR_UNFREEZE, frozen.action)
    }

    @Test
    fun packageRefreshPreservesExpansionState() {
        val expandedState = HomeFunctionZoneState()
        expandedState.toggleExpanded()
        expandedState.setPackageFrozen(true)

        assertTrue(expandedState.expanded)
        assertEquals(16, expandedState.items().size)
        assertEquals("解冻套餐", expandedState.items()[1].title)

        val collapsedState = HomeFunctionZoneState()
        collapsedState.setPackageFrozen(true)

        assertFalse(collapsedState.expanded)
        assertEquals(10, collapsedState.items().size)
    }

    @Test
    fun iconUrlContainsEncodedChineseFileName() {
        val items = buildHomeFunctionItems(expanded = true, packageFrozen = true)

        items.forEach { item ->
            assertTrue(item.iconName.isNotBlank())
            assertFalse(homeFunctionIconUrl(item.iconName).contains("首页"))
        }
        assertEquals(
            "https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/%E9%A6%96%E9%A1%B5-%E7%94%B5%E6%B1%A0%E8%A2%AB%E5%90%9E.png",
            homeFunctionIconUrl("电池被吞")
        )
    }
}
