package com.utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

public class TextUtil {

    public static String formatTime(String startTime,String endTime){
        if (TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime)){
            return "暂无";
        }else if (startTime.length() > 10 && endTime.length() > 10){
            return startTime.replace("-","/").substring(0,10) + "至"+ endTime.replace("-","/").substring(0,10);
        }else {
            return startTime + "至" + endTime;
        }
    }

    public static SpannableString getSpannableString(String[] text,String[] colors){
        if (text.length != colors.length){
            return new SpannableString("");
        }
        String s = "";
        for (String t:text){
            s = s + t;
        }
        SpannableString spannableString = new SpannableString(s);
        int startPos = 0;
        for (int i = 0; i < text.length; i++) {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor(colors[i]));
            spannableString.setSpan(colorSpan, startPos, startPos+text[i].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            startPos += text[i].length() ;
        }
        return spannableString;
    }
}
