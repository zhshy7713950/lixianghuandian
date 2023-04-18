package com.ruimeng.things;

import java.text.DecimalFormat;

public class MathUtil {

    /**
     * 保留两位小数
     * @param number
     * @return
     */
    public static String reservedDecimal(double number) {
        String getNumber;
        if (0 == number) {
            getNumber = "0";
        } else {
            DecimalFormat df = new DecimalFormat("#.00");
            getNumber = df.format(number);
        }
        if (getNumber.startsWith(".")){
            getNumber="0"+getNumber;
        }
        return getNumber;
    }

}
