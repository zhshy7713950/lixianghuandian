package com.utils

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import android.view.View
import com.utils.ColorHelper.getColor

object StatusBarHelper {

    fun setTopColor(activity: Activity?, color: Int) {
        com.jaeger.library.StatusBarUtil.setColor(activity, getColor(activity!!, color), 0)
    }

    fun setTopColor(activity: Activity?, color: Int, @IntRange(from = 0, to = 255) statusBarAlpha: Int) {
        com.jaeger.library.StatusBarUtil.setColor(activity, getColor(activity!!, color), statusBarAlpha)
    }

    fun setTopBbackGround(activity: Activity?, view: View?, @DrawableRes id: Int) {
        view!!.background = ContextCompat.getDrawable(activity!!, id)
        StatusBarUtil.setTransparentForWindow(activity)
    }

    fun setTopColor(activity: Activity?, view: View?, colors: IntArray) {
        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        view!!.background = gradientDrawable
        StatusBarUtil.setTransparentForWindow(activity!!)
    }

    fun setTopColor(view: View?, colors: IntArray) {
        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        view!!.background = gradientDrawable
    }

    fun getColor(startColor: String, endColor: String): IntArray? {
        val mStartColor = -0x1000000 or startColor.replace("#", "").toInt(16)
        val mEndColor = -0x1000000 or endColor.replace("#", "").toInt(16)
        return intArrayOf(mStartColor, mEndColor)
    }
    

}