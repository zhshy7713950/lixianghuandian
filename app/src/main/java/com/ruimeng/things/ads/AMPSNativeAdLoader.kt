package com.ruimeng.things.ads

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import xyz.adscope.amps.ad.nativead.AMPSNativeAd
import xyz.adscope.amps.ad.nativead.AMPSNativeLoadEventListener
import xyz.adscope.amps.ad.nativead.adapter.AMPSNativeAdExpressListener
import xyz.adscope.amps.ad.nativead.inter.AMPSNativeAdExpressInfo
import xyz.adscope.amps.common.AMPSError
import xyz.adscope.amps.config.AMPSRequestParameters
import xyz.adscope.amps.tool.util.AMPSScreenUtil

/**
 * AMPS 原生广告加载器
 *
 * 目标：
 * - 对外暴露简单的 load 方法
 * - 自动管理生命周期（外部传入 Lifecycle）
 * - 传入容器，自动渲染广告视图
 * - 暴露回调接口供业务监听
 *
 * 用法：
 * val loader = AMPSNativeAdLoader(activity, lifecycle)
 * loader.loadInto(container, spaceId = "xxxx", widthPx = 0, heightPx = 0, listener = ...)
 *
 * 设计遵循：
 * - 单一职责：仅负责原生广告的创建、加载、渲染与销毁
 * - 明确错误处理：所有回调路径均捕获并上报
 */
class AMPSNativeAdLoader(
    private val activity: Activity,
    lifecycle: Lifecycle
) : LifecycleObserver {

    interface Listener {
        fun onLoadSuccess(infoList: List<AMPSNativeAdExpressInfo>) {}
        fun onLoadFailed(errorCode: Int, message: String?) {}
        fun onAdShow() {}
        fun onAdClicked() {}
        fun onAdClosed(view: View?) {}
        fun onRenderFail(view: View?, msg: String?, code: Int) {}
        fun onRenderSuccess(view: View, width: Float, height: Float) {}
    }

    companion object {
        private const val TAG = "AMPSNativeAdLoader"
    }
    
    /**
     * 获取默认广告宽度：屏幕宽度 - 24dp（左右各12dp边距）
     */
    private fun getDefaultAdWidth(): Int {
        val screenWidth = AMPSScreenUtil.getScreenWidth(activity)
        val marginDp = 24f // 左右各12dp
        val marginPx = marginDp * activity.resources.displayMetrics.density
        return (screenWidth - marginPx).toInt()
    }

    private var nativeAd: AMPSNativeAd? = null
    private var lastAdView: View? = null
    private var targetContainer: ViewGroup? = null
    private var externalListener: Listener? = null
    private var cornerRadius: Float = 10f // 默认圆角半径

    init {
        lifecycle.addObserver(this)
    }

    /**
     * 加载并渲染到指定容器
     * @param container 承载广告 View 的容器
     * @param spaceId 广告位 ID（来自 AdScope 后台）
     * @param widthPx 广告期望宽度，传 0 或屏幕宽度表示自适应宽度
     * @param heightPx 广告期望高度，传 0 表示自适应高度
     * @param adCount 请求数量，默认 1
     * @param timeoutMs 超时时间，默认 5000ms
     */
    fun loadInto(
        container: ViewGroup,
        spaceId: String = AdManager.NATIVE_SPACE_ID,
        widthPx: Int = getDefaultAdWidth(),
        heightPx: Int = 0,
        adCount: Int = 1,
        timeoutMs: Int = 5000,
        cornerRadius: Float = 10f, // 添加圆角参数，默认10dp
        listener: Listener? = null
    ) {
        if (!AdManager.getInstance().isAdEnabled()) {
            Log.w(TAG, "广告总开关关闭，跳过加载")
            listener?.onLoadFailed(-1, "ad switch is off")
            return
        }
        if (!AdManager.getInstance().isSdkInitialized()) {
            Log.w(TAG, "广告SDK未初始化，跳过加载")
            listener?.onLoadFailed(-2, "sdk not initialized")
            return
        }

        this.targetContainer = container
        this.externalListener = listener
        this.cornerRadius = cornerRadius

        val params = AMPSRequestParameters.Builder()
            .setSpaceId(spaceId)
            .setTimeOut(timeoutMs)
            .setWidth(if (widthPx <= 0) AMPSScreenUtil.getScreenWidth(activity) else widthPx)
            .setHeight(if (heightPx < 0) 0 else heightPx)
            .setAdCount(if (adCount <= 0) 1 else adCount)
            .build()

        nativeAd?.destroy()
        nativeAd = AMPSNativeAd(activity, params, object : AMPSNativeLoadEventListener() {
            override fun onAmpsAdLoad(resultList: MutableList<AMPSNativeAdExpressInfo>?) {
                if (resultList.isNullOrEmpty()) {
                    Log.e(TAG, "onAmpsAdLoad but empty result")
                    externalListener?.onLoadFailed(-3, "empty result")
                    return
                }
                externalListener?.onLoadSuccess(resultList)
                val first = resultList[0]
                bindExpressListener(first)
                try {
                    first.render()
                } catch (e: Exception) {
                    Log.e(TAG, "render exception", e)
                    externalListener?.onRenderFail(null, e.message, -4)
                }
            }

            override fun onAmpsAdFailed(ampsError: AMPSError?) {
//                val code = ampsError?.code?.toInt() ?: -5
//                val msg = ampsError?.message
//                Log.e(TAG, "load failed code=$code msg=$msg")
//                externalListener?.onLoadFailed(code, msg)
            }
        })

        try {
            nativeAd?.loadAd()
        } catch (e: Exception) {
            Log.e(TAG, "loadAd exception", e)
            externalListener?.onLoadFailed(-6, e.message)
        }
    }

    private fun bindExpressListener(info: AMPSNativeAdExpressInfo) {
        info.setAMPSNativeAdExpressListener(object : AMPSNativeAdExpressListener() {
            override fun onAdShow() {
                Log.d(TAG, "onAdShow")
                externalListener?.onAdShow()
            }

            override fun onAdClicked() {
                Log.d(TAG, "onAdClicked")
                externalListener?.onAdClicked()
            }

            override fun onAdClosed(view: View) {
                Log.d(TAG, "onAdClosed")
                removeFromContainer(view)
                externalListener?.onAdClosed(view)
            }

            override fun onRenderFail(view: View, msg: String, code: Int) {
                Log.e(TAG, "onRenderFail code=$code msg=$msg")
                externalListener?.onRenderFail(view, msg, code)
            }

            override fun onRenderSuccess(view: View, width: Float, height: Float) {
                attachToContainer(view)
                externalListener?.onRenderSuccess(view, width, height)
            }
        })
    }

    private fun attachToContainer(view: View) {
        val container = targetContainer ?: return
        safeRemoveFromParent(view)
        container.removeAllViews()
        
        // 设置广告View的圆角
        setViewCornerRadius(view, cornerRadius)
        
        container.addView(view)
        lastAdView = view
    }
    
    /**
     * 设置View的圆角
     * @param view 要设置圆角的View
     * @param radius 圆角半径（dp）
     */
    private fun setViewCornerRadius(view: View, radius: Float) {
        try {
            // 将dp转换为px
            val radiusPx = radius * activity.resources.displayMetrics.density
            
            // 创建圆角背景
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = radiusPx
//                setColor(android.graphics.Color.WHITE)
//                setStroke(1, android.graphics.Color.parseColor("#F0F0F0"))
            }
            
            // 设置背景
            view.background = drawable
            
            // 设置裁剪，确保子View也被圆角裁剪
            view.clipToOutline = true
            view.outlineProvider = object : android.view.ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, radiusPx)
                }
            }
            
            Log.d(TAG, "设置广告View圆角成功，半径: ${radiusPx}px")
        } catch (e: Exception) {
            Log.e(TAG, "设置View圆角失败", e)
        }
    }

    private fun removeFromContainer(view: View?) {
        val container = targetContainer ?: return
        val target = view ?: lastAdView ?: return
        safeRemoveFromParent(target)
    }

    private fun safeRemoveFromParent(view: View) {
        val parent = view.parent
        if (parent is ViewGroup) {
            parent.removeView(view)
        }
    }

    // ===== Lifecycle =====
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        try {
            nativeAd?.resume()
        } catch (e: Exception) {
            Log.e(TAG, "resume exception", e)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        try {
            nativeAd?.destroy()
            nativeAd = null
            lastAdView = null
            targetContainer = null
            externalListener = null
        } catch (e: Exception) {
            Log.e(TAG, "destroy exception", e)
        }
    }
}
