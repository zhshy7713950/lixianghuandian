package com.utils

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import java.lang.Exception

object TextUtil {
    fun formatTime(startTime: String?, endTime: String?): String {
        try {
            val time =  if (startTime== null || endTime == null) {
                "暂无"
            } else if (startTime.length > 10 && endTime.length > 10) {
                startTime.replace("-", "/").substring(0, 10) + "至" + endTime.replace("-", "/")
                    .substring(0, 10)
            } else {
                startTime + "至" + endTime
            }
            return time
        }catch (e :Exception){
            e.printStackTrace()
            return  "暂无"
        }


    }

    fun getSpannableString(text: Array<String>, colors: Array<String> = arrayOf("#929FAB","#FFFFFF")): SpannableString {
        try {
            if (text.size != colors.size) {
                return SpannableString("")
            }
            var s = ""
            for (t in text) {
                s = s + t
            }
            val spannableString = SpannableString(s)
            var startPos = 0
            for (i in text.indices) {
                if (!TextUtils.isEmpty(text[i])) {
                    val colorSpan = ForegroundColorSpan(Color.parseColor(colors[i]))
                    spannableString.setSpan(
                        colorSpan,
                        startPos,
                        startPos + text[i].length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    startPos += text[i].length
                }
            }
            return spannableString
        }catch (e :Exception){
            e.printStackTrace()
            return SpannableString("");
        }

    }

    fun  getMoneyText(text :String): SpannableStringBuilder {
        var builder = SpannableStringBuilder()
        val tag = SpannableString("¥")
        tag.setSpan(RelativeSizeSpan(0.7f),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(tag)
        builder.append(text)
        return builder

    }
}