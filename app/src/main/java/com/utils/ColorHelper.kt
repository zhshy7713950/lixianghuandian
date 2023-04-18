package com.utils

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat

object ColorHelper {

    fun getColor(@NonNull context: Context, @ColorRes id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

}