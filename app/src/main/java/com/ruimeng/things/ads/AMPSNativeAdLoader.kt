package com.ruimeng.things.ads

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import wongxd.common.dp2px
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
        val marginPx = 24.dp2px
        return (screenWidth - marginPx).toInt()
    }

    private fun getDefaultAdHeight(): Int {
        return 120.dp2px.toInt()
    }

    private var nativeAd: AMPSNativeAd? = null
    private var lastAdView: View? = null
    private var targetContainer: ViewGroup? = null
    private var externalListener: Listener? = null
    private var cornerRadius: Float = 10f // 默认圆角半径

    init {
        lifecycle.addObserver(this)
    }

    fun commonLoadInto(container: ViewGroup,
                       spaceId: String = AdManager.NATIVE_SPACE_ID_HOME){
        loadInto(
            container = container,
            spaceId = spaceId,
            listener = object : Listener {
                override fun onLoadSuccess(infoList: List<AMPSNativeAdExpressInfo>) {
                }

                override fun onRenderSuccess(view: View, width: Float, height: Float) {
                    // 广告渲染成功，保持显示
                }

                override fun onLoadFailed(errorCode: Int, message: String?) {
                    // 广告加载失败，隐藏容器
                    container.visibility = View.GONE
                }

                override fun onAdClosed(view: View?) {
                    // 广告被关闭（点击X按钮），隐藏容器
                    container.visibility = View.GONE
                }

                override fun onAdShow() {
                    // 广告展示，可以添加埋点
                }

                override fun onAdClicked() {
                    // 广告被点击，可以添加埋点
                }
            }
        )
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
        spaceId: String = AdManager.NATIVE_SPACE_ID_HOME,
        widthPx: Int = getDefaultAdWidth(),
        heightPx: Int = getDefaultAdHeight(),
        adCount: Int = 1,
        timeoutMs: Int = 5000,
        cornerRadius: Float = 10f, // 添加圆角参数，默认10dp
        listener: Listener? = null
    ) {
        if (!AdManager.getInstance().isAdEnabled()) {
            listener?.onLoadFailed(-1, "ad switch is off")
            return
        }
        if (!AdManager.getInstance().isSdkInitialized()) {
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
            externalListener?.onLoadFailed(-6, e.message)
        }
    }

    private fun bindExpressListener(info: AMPSNativeAdExpressInfo) {
        info.setAMPSNativeAdExpressListener(object : AMPSNativeAdExpressListener() {
            override fun onAdShow() {
                externalListener?.onAdShow()
            }

            override fun onAdClicked() {
                externalListener?.onAdClicked()
            }

            override fun onAdClosed(view: View) {
                removeFromContainer(view)
                externalListener?.onAdClosed(view)
            }

            override fun onRenderFail(view: View, msg: String, code: Int) {
                externalListener?.onRenderFail(view, msg, code)
            }

            override fun onRenderSuccess(view: View, width: Float, height: Float) {
                attachToContainer(view, width, height)
                externalListener?.onRenderSuccess(view, width, height)
            }
        })
    }

    private fun attachToContainer(view: View, width: Float, height: Float) {
        val container = targetContainer ?: return
        safeRemoveFromParent(view)
        container.removeAllViews()

        // 设置广告View的圆角
        setViewCornerRadius(view, cornerRadius)

        // 计算最终高度
        val finalHeight = calculateFinalHeight(width, height)
        
        // 如果计算出的高度为0，则不添加view
        if (finalHeight <= 0) {
            container.visibility = View.GONE
            return
        }

        val vlp = LayoutParams(LayoutParams.MATCH_PARENT, finalHeight)
        container.addView(view, vlp)
        lastAdView = view
        // 广告加载成功，显示容器
        container.visibility = View.VISIBLE
    }
    
    /**
     * 计算广告View的最终高度
     * @param width 广告原始宽度
     * @param height 广告原始高度
     * @return 计算后的最终高度，如果为0表示不显示广告
     */
    private fun calculateFinalHeight(width: Float, height: Float): Int {
        // 1. 入参高度为0，直接不添加view
        if (height <= 0) {
            return 0
        }
        
        val defaultHeight = getDefaultAdHeight()
        val defaultWidth = getDefaultAdWidth()
        
        // 2. 入参高度小于getDefaultAdHeight()，则根据入参宽高比及getDefaultAdWidth()计算出等比例的高度
        if (height < defaultHeight) {
            return if (width > 0) {
                // 计算宽高比
                val aspectRatio = height / width
                // 根据默认宽度和宽高比计算等比例高度
                val calculatedHeight = (defaultWidth * aspectRatio).toInt()
                calculatedHeight
            } else {
                // 如果宽度也为0，使用默认高度
                defaultHeight
            }
        }
        
        // 3. 入参高度和getDefaultAdHeight()相同，直接把入参高度给View
        if (height == defaultHeight.toFloat()) {
            return height.toInt()
        }
        
        // 4. 入参高度大于默认高度，也直接使用（保持原有逻辑）
        return height.toInt()
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
            
        } catch (e: Exception) {
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
