package com.utils

import android.provider.ContactsContract.Data
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fgt_pay_rent_money.rv_change_package
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

inline fun <T> unsafeLazy(crossinline initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) {
        initializer()
    }

val pass = {}

val dateFormat: DateFormat = SimpleDateFormat("MM.dd HH:mm:ss")

fun Any.curDateByFormat(): String = dateFormat.format(Date())

inline fun String?.isZero() = "0" == this || "0.00" == this
fun String?.safeToInt(): Int {
    return try {
        this?.toInt() ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

fun String?.safeToFloat(): Float {
    return try {
        this?.toFloat() ?: 0f
    } catch (e: Exception) {
        e.printStackTrace()
        0f
    }
}

fun RecyclerView.fixHeight(dataSize: Int, spanCount: Int = 2) {
    post {
        if (childCount > 0) {
            // 获取第一个子项的高度
            val firstChild = getChildAt(0)
            val itemHeight = firstChild.height + firstChild.marginTop + firstChild.marginBottom

            // 计算总行数（向上取整）
            val totalRows = kotlin.math.ceil(dataSize.toDouble() / spanCount).toInt()

            // 计算总高度（考虑行间距，如果有）
            val spacing = (layoutManager as GridLayoutManager)
                .getTopDecorationHeight(firstChild)
            val totalHeight = (itemHeight + spacing) * totalRows

            // 更新 RecyclerView 高度
            layoutParams.height = totalHeight
            requestLayout()
        }
    }
}
