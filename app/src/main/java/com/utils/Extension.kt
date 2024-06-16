package com.utils

inline fun <T> unsafeLazy(crossinline initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) {
        initializer()
    }

val pass = {}

inline fun String.isZero() = "0" == this || "0.00" == this
