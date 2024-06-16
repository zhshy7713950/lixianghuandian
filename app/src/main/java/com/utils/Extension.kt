package com.utils

inline fun <T> unsafeLazy(crossinline initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) {
        initializer()
    }

val pass = {}

inline fun String?.isZero() = "0" == this || "0.00" == this
fun String?.safeToInt(): Int {
    try {
        return this?.toInt() ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        return 0
    }
}
