package wongxd.common


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.*
import android.widget.Button


@SuppressLint("StaticFieldLeak")
/**
 * Created by wongxd on 2018/06/13.
 *            https://github.com/wongxd
 *            wxd1@live.com
 *
 */
class StatusLayout private constructor(contentView: ViewGroup) {

    private val mContext: Context = contentView.context
    private val contentLayout: ViewGroup = contentView

    private lateinit var emptyLayout: ViewGroup
    private lateinit var loadingLayout: ViewGroup
    private lateinit var networkErrorLayout: ViewGroup
    private var currentLayout: ViewGroup? = null
    private var isAresShowing = false
    private var onRetryClickedListener: OnRetryClickedListener? = null


    companion object {


        /**
         * 初始化
         *
         * @param content 原始view
         */
        fun init(content: ViewGroup): StatusLayout {
            return StatusLayout(content)
        }
    }


    /**
     * 设置空数据界面的布局
     */
    fun setEmptyLayout(resId: Int) {
        emptyLayout = getLayout(resId)
        emptyLayout.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * 设置加载中界面的布局
     */
    fun setLoadingLayout(resId: Int) {
        loadingLayout = getLayout(resId)
        loadingLayout.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * 设置网络错误界面的布局
     */
    fun setNetworkErrorLayout(resId: Int, btnResId: Int) {
        networkErrorLayout = getLayout(resId)
        networkErrorLayout.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        networkErrorLayout.findViewById<Button>(btnResId).setOnClickListener {
            onRetryClickedListener?.onRetryClick()
        }
    }

    /**
     * 展示空数据界面
     * target的大小及位置决定了window界面在实际屏幕中的展示大小及位置
     */
    fun showEmptyLayout(wm: WindowManager) {
        if (currentLayout != null) {
            wm.removeView(currentLayout)
        }
        isAresShowing = true
        currentLayout = emptyLayout
        wm.addView(currentLayout, setLayoutParams(contentLayout))
    }

    /**
     * 展示加载中界面
     * target的大小及位置决定了window界面在实际屏幕中的展示大小及位置
     */
    fun showLoadingLayout(wm: WindowManager) {
        if (currentLayout != null) {
            wm.removeView(currentLayout)
        }
        isAresShowing = true
        currentLayout = loadingLayout
        wm.addView(currentLayout, setLayoutParams(contentLayout))
    }

    /**
     * 展示网络错误界面
     * target的大小及位置决定了window界面在实际屏幕中的展示大小及位置
     */
    fun showNetworkErrorLayout(wm: WindowManager) {
        if (currentLayout != null) {
            wm.removeView(currentLayout)
        }
        isAresShowing = true
        currentLayout = networkErrorLayout
        wm.addView(currentLayout, setLayoutParams(contentLayout))
    }

    fun getEmptyLayout(): ViewGroup = emptyLayout


    fun showContent(wm: WindowManager) {

        if (currentLayout != null) {
            wm.removeView(currentLayout)
        }
        isAresShowing = true
        currentLayout = contentLayout
        wm.addView(currentLayout, setLayoutParams(contentLayout))

    }

    /**
     * 是否有Ares界面正在展示
     */
    fun isAresShowing(): Boolean = isAresShowing


    private fun setLayoutParams(target: View): WindowManager.LayoutParams {
        val wlp = WindowManager.LayoutParams()
        wlp.format = PixelFormat.TRANSPARENT
        wlp.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        val location = IntArray(2)
        target.getLocationOnScreen(location)
        wlp.x = location[0]
        wlp.y = location[1]
        wlp.height = target.height
        wlp.width = target.width
        wlp.type = WindowManager.LayoutParams.FIRST_SUB_WINDOW
        wlp.gravity = Gravity.LEFT or Gravity.TOP
        return wlp
    }

    private fun getLayout(resId: Int): ViewGroup {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return inflater.inflate(resId, null) as ViewGroup
    }

    interface OnRetryClickedListener {
        fun onRetryClick()
    }

    fun setOnRetryClickedListener(listener: OnRetryClickedListener) {
        onRetryClickedListener = listener
    }

    fun onDestroy(wm: WindowManager) {
        isAresShowing = false
        currentLayout?.let {
            wm.removeView(currentLayout)
            currentLayout = null
        }
    }
}