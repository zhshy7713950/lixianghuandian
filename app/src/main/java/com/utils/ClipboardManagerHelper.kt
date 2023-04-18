package com.utils

import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils

object ClipboardManagerHelper {

    @Suppress("DEPRECATION")
    fun copy(context: Context?, content: String, successMsg: String, failMsg: String) {
        if (!TextUtils.isEmpty(content)) {
            val clipboardManager: ClipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.text = content
            ToastHelper.shortToast(context, successMsg)
        } else {
            ToastHelper.shortToast(context, failMsg)
        }
    }

}