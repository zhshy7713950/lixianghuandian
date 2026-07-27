package com.ruimeng.things.ads

import android.app.Application
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.ruimeng.things.App
import org.greenrobot.eventbus.EventBus
import wongxd.Config
import wongxd.Wongxd
import xyz.adscope.amps.AMPSSDK
import xyz.adscope.amps.ad.nativead.AMPSNativeAd
import xyz.adscope.amps.ad.nativead.AMPSNativeLoadEventListener
import xyz.adscope.amps.ad.nativead.adapter.AMPSNativeAdExpressListener
import xyz.adscope.amps.ad.nativead.inter.AMPSNativeAdExpressInfo
import xyz.adscope.amps.common.AMPSError
import xyz.adscope.amps.config.AMPSPrivacyConfig
import xyz.adscope.amps.config.AMPSRequestParameters
import xyz.adscope.amps.init.AMPSInitConfig
import xyz.adscope.amps.init.inter.IAMPSInitCallback
import xyz.adscope.amps.tool.util.AMPSScreenUtil

/**
 * 广告管理单例类
 * 
 * 功能说明：
 * - 管理全局广告开关状态
 * - 提供广告显示控制逻辑
 * - 存储广告相关配置信息
 * 
 * 使用方法：
 * 1. 通过AdManager.getInstance()获取单例实例
 * 2. 调用setAdEnabled()设置广告开关状态
 * 3. 调用isAdEnabled()检查广告是否允许显示
 * 
 * @author 理想换电开发团队
 * @version 1.0.0
 * @since 2024年
 */
class AdManager private constructor() {
    
    companion object {
        private const val TAG = "AdManager"
        
        @Volatile
        private var INSTANCE: AdManager? = null

        // AdScope AppId（需要从AdScope开发者后台获取）
        const val AMPS_APPID = "55292" // 请替换为实际的AppId

        const val NATIVE_SPACE_ID_HOME: String = "122010"
        const val NATIVE_SPACE_ID_STATION: String = "122012"
        const val NATIVE_SPACE_ID_ME: String = "122011"
        const val NATIVE_SPACE_ID_CHANGE: String = "122009"

        /**
         * 获取AdManager单例实例
         * 
         * @return AdManager实例
         */
        fun getInstance(): AdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdManager().also { INSTANCE = it }
            }
        }
    }
    
    // 广告开关状态：true-允许显示，false-禁止显示
    private var isAdEnabled: Boolean = false
    
    // 广告开关状态是否已初始化
    private var isInitialized: Boolean = false
    
    // 广告SDK是否已初始化
    private var isSdkInitialized: Boolean = false

    /**
     * 应用商店审核中状态：
     * APP 版本号高于后管平台版本号时为 true，此时强制隐藏三方广告 Banner
     */
    private var isUnderReview: Boolean = false



    /**
     * 设置广告开关状态
     * 
     * @param enabled true表示允许显示广告，false表示禁止显示广告
     */
    fun setAdEnabled(enabled: Boolean) {
        this.isAdEnabled = enabled
        this.isInitialized = true
        Log.d(TAG, "广告开关状态已设置: $enabled, 审核中=$isUnderReview")
        
        // 如果广告开关打开且SDK未初始化，则初始化SDK（审核中不初始化）
        if (enabled && !isUnderReview && !isSdkInitialized) {
            initAdSdk()
        }
    }

    /**
     * 设置是否处于应用商店审核中
     *
     * @param underReview true=审核中（隐藏广告），false=已上线（可显示广告）
     */
    fun setUnderReview(underReview: Boolean) {
        this.isUnderReview = underReview
        Log.d(TAG, "应用商店审核状态已设置: underReview=$underReview")
        // 若已判定可展示广告且 SDK 未初始化，补一次初始化
        if (!underReview && isAdEnabled && !isSdkInitialized) {
            initAdSdk()
        }
    }

    /**
     * 当前是否处于应用商店审核中
     */
    fun isUnderReview(): Boolean = isUnderReview
    
    /**
     * 检查广告是否允许显示
     * 
     * @return true表示允许显示广告，false表示禁止显示广告
     */
    fun isAdEnabled(): Boolean {
        if (!isInitialized) {
            Log.w(TAG, "广告开关状态未初始化，默认禁止显示广告")
            return false
        }
        if (isUnderReview) {
            Log.d(TAG, "应用商店审核中，隐藏广告 Banner")
            return false
        }
        return isAdEnabled
    }

    /**
     * 比较版本号：appVer > platformVer 返回正数
     * 例：1.0.43 vs 1.0.42 → 1（审核中）
     */
    fun compareVersion(appVer: String, platformVer: String): Int {
        val appParts = appVer.trim().removePrefix("v").removePrefix("V")
            .split(".").map { it.toIntOrNull() ?: 0 }
        val platformParts = platformVer.trim().removePrefix("v").removePrefix("V")
            .split(".").map { it.toIntOrNull() ?: 0 }
        val len = maxOf(appParts.size, platformParts.size)
        for (i in 0 until len) {
            val a = appParts.getOrElse(i) { 0 }
            val b = platformParts.getOrElse(i) { 0 }
            if (a != b) return a.compareTo(b)
        }
        return 0
    }
    
    /**
     * 检查广告开关是否已初始化
     * 
     * @return true表示已初始化，false表示未初始化
     */
    fun isInitialized(): Boolean {
        return isInitialized
    }
    
    /**
     * 检查广告SDK是否已初始化
     * 
     * @return true表示SDK已初始化，false表示SDK未初始化
     */
    fun isSdkInitialized(): Boolean {
        return isSdkInitialized
    }
    
    /**
     * 初始化广告SDK
     * 
     * 当广告开关打开且SDK未初始化时调用
     */
    private fun initAdSdk() {
        try {
            Log.d(TAG, "开始初始化AdScope广告SDK")
            
            val application = Wongxd.instance
            if (application == null) {
                Log.e(TAG, "无法获取Application实例，SDK初始化失败")
                return
            }
            
            // 创建初始化配置
            val config = AMPSInitConfig.Builder()
                .setAppId(AMPS_APPID)
                .setAppName("锂享换电")
                .setAMPSPrivacyConfig(object : AMPSPrivacyConfig() {
                    override fun isCanUsePhoneState(): Boolean {
                        return super.isCanUsePhoneState()
                    }
                })
                .build()
            
            // 初始化SDK
            AMPSSDK.init(application, config, object : IAMPSInitCallback {
                override fun successCallback() {
                    isSdkInitialized = true
                    Log.i(TAG, "AdScope广告SDK初始化成功")
                    // 发送SDK初始化成功事件
                    EventBus.getDefault().post(AdSdkInitSuccessEvent())
                }
                
                override fun failCallback(ampsError: AMPSError) {
                    isSdkInitialized = false
                    Log.e(TAG, "AdScope广告SDK初始化失败: ${ampsError.toString()}")
                }
            })
            
        } catch (e: Exception) {
            isSdkInitialized = false
            Log.e(TAG, "AdScope广告SDK初始化异常", e)
        }
    }


}
