package com.ruimeng.things.utils

import com.ruimeng.things.FgtMain
import me.yokeyword.fragmentation.ISupportFragment
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus

/**
 * 页面导航工具类
 * 提供安全的页面跳转和返回方法，解决页面栈管理问题
 * 
 * 遵循SOLID原则：
 * - 单一职责：专门负责页面导航逻辑
 * - 开闭原则：可以扩展新的导航方法而不修改现有代码
 */
object PageNavigationHelper {
    
    /**
     * 返回到主页面并切换tab（智能检测方案）
     * 使用页面栈深度检测，智能选择返回策略
     * @param targetTab 目标tab索引 (0:首页, 1:网点, 2:优惠活动, 3:我的)
     * @param currentFragment 当前Fragment实例
     */
    fun backToMainAndSwitchTab(targetTab: Int, currentFragment: SupportFragment) {
        try {
            // 1. 先发送EventBus事件切换tab
            EventBus.getDefault().post(FgtMain.SwitchPageEvent(targetTab))
            
            currentFragment.popTo(FgtMain::class.java, false)
            
        } catch (e: Exception) {
            // 异常处理：记录错误并尝试备选方案
            e.printStackTrace()
            try {
                // 备选方案：使用start启动主页面
                currentFragment.start(FgtMain())
            } catch (startException: Exception) {
                startException.printStackTrace()
            }
        }
    }

    
    /**
     * 安全启动新页面
     * @param targetFragment 目标Fragment实例
     * @param currentFragment 当前Fragment实例
     */
    fun safeStartFragment(targetFragment: ISupportFragment, currentFragment: SupportFragment) {
        try {
            currentFragment.start(targetFragment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
