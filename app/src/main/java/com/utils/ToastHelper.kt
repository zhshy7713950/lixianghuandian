package com.utils

import android.content.Context
import android.widget.Toast


object ToastHelper {

    fun longToast(context: Context?, charSequence: CharSequence?) {
        Toast.makeText(context, charSequence, Toast.LENGTH_LONG).show()
    }

    fun shortToast(context: Context?, charSequence: CharSequence?) {
        Toast.makeText(context, charSequence, Toast.LENGTH_SHORT).show()
    }


}