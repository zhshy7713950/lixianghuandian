package com.utils

import androidx.annotation.IdRes

interface CommonDialogCallBackHelper {

    fun back(@IdRes viewId: Int, msg: String?)

}