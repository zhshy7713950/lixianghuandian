package com.utils

import android.content.res.Resources

class DensityHelper {

    private var density: Float = Resources.getSystem().displayMetrics.density

    companion object {
        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
        fun dp2px(dpValue: Float): Int {
            return (0.5f + dpValue * Resources.getSystem().displayMetrics.density).toInt()
        }

        /**
         * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
         */
        fun px2dp(pxValue: Float): Float {
            return pxValue / Resources.getSystem().displayMetrics.density
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(dpValue: Float): Int {
        return (0.5f + dpValue * density).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(pxValue: Float): Float {
        return pxValue / density
    }

}