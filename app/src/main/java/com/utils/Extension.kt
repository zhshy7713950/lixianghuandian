package com.utils

inline fun <T> unsafeLazy(crossinline initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) {
        initializer()
    }

val pass = {}

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
