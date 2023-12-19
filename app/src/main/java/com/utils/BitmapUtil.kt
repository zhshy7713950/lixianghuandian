package com.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import wongxd.utils.utilcode.subutil.util.ThreadPoolUtils.Type

class BitmapUtil {
    fun overlayTextOnImage(context: Context, imageResId: Int, text: String, color:Int): Bitmap {
        // 获取原始图片
        val originalDrawable = context.resources.getDrawable(imageResId, null) as BitmapDrawable
        val originalBitmap = originalDrawable.bitmap

        // 创建一个空的Bitmap作为画布
        val resultBitmap = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            originalBitmap.config
        )

        // 创建Canvas对象，并将原始Bitmap绘制在上面
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        // 创建一个Paint对象用于绘制文本
        val paint = Paint()
        paint.color = color
        paint.textSize = DensityUtil.dip2px(14f,context).toFloat()
        paint.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        paint.isAntiAlias = true
        val width = DensityUtil.dip2px(14f,context).toFloat() / 2


        // 计算文本的位置（居中）
        val textX = (originalBitmap.width - paint.measureText(text) ) / 2
        val textY = (originalBitmap.height - width  ) / 2

        // 绘制文本到Canvas
        canvas.drawText(text, textX, textY, paint)

        return resultBitmap
    }
}