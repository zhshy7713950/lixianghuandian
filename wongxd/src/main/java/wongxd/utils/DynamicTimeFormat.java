package wongxd.utils;


import androidx.annotation.NonNull;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 动态时间格式化
 */

public class DynamicTimeFormat extends SimpleDateFormat {

    private static Locale locale = Locale.CHINA;
    private static String weeks[] = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    private static String moments[] = {"中午", "凌晨", "早上", "下午", "晚上"};

    private String mFormat = "%s";

    public DynamicTimeFormat() {
        this("%s", "yyyy年", "M月d日", "HH:mm");
    }

    public DynamicTimeFormat(String format) {
        this();
        this.mFormat = format;
    }

    public DynamicTimeFormat(String yearFormat, String dateFormat, String timeFormat) {
        super(String.format(locale, "%s %s %s", yearFormat, dateFormat, timeFormat), locale);
    }

    public DynamicTimeFormat(String format, String yearFormat, String dateFormat, String timeFormat) {
        this(yearFormat, dateFormat, timeFormat);
        this.mFormat = format;
    }

    @Override
    public StringBuffer format(@NonNull Date date, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
        toAppendTo = super.format(date, toAppendTo, pos);
        Calendar otherCalendar = calendar;
        Calendar todayCalendar = Calendar.getInstance();

        int hour = otherCalendar.get(Calendar.HOUR_OF_DAY);

        String[] times = toAppendTo.toString().split(" ");
        String moment = hour == 12 ? moments[0] : moments[hour / 6 + 1];
        String timeFormat = moment + " " + times[2];
        String dateFormat = times[1] + " " + timeFormat;
        String yearFormat = times[0] + dateFormat;
        toAppendTo.delete(0, toAppendTo.length());

        boolean yearTemp = todayCalendar.get(Calendar.YEAR) == otherCalendar.get(Calendar.YEAR);
        if (yearTemp) {
            int todayMonth = todayCalendar.get(Calendar.MONTH);
            int otherMonth = otherCalendar.get(Calendar.MONTH);
            if (todayMonth == otherMonth) {//表示是同一个月
                int temp = todayCalendar.get(Calendar.DATE) - otherCalendar.get(Calendar.DATE);
                switch (temp) {
                    case 0:
                        toAppendTo.append(timeFormat);
                        break;
                    case 1:
                        toAppendTo.append("昨天 ");
                        toAppendTo.append(timeFormat);
                        break;
                    case 2:
                        toAppendTo.append("前天 ");
                        toAppendTo.append(timeFormat);
                        break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        int dayOfMonth = otherCalendar.get(Calendar.WEEK_OF_MONTH);
                        int todayOfMonth = todayCalendar.get(Calendar.WEEK_OF_MONTH);
                        if (dayOfMonth == todayOfMonth) {//表示是同一周
                            int dayOfWeek = otherCalendar.get(Calendar.DAY_OF_WEEK);
                            if (dayOfWeek != 1) {//判断当前是不是星期日     如想显示为：周日 12:09 可去掉此判断
                                toAppendTo.append(weeks[otherCalendar.get(Calendar.DAY_OF_WEEK) - 1]);
                                toAppendTo.append(' ');
                                toAppendTo.append(timeFormat);
                            } else {
                                toAppendTo.append(dateFormat);
                            }
                        } else {
                            toAppendTo.append(dateFormat);
                        }
                        break;
                    default:
                        toAppendTo.append(dateFormat);
                        break;
                }
            } else {
                toAppendTo.append(dateFormat);
            }
        } else {
            toAppendTo.append(yearFormat);
        }

        int length = toAppendTo.length();
        toAppendTo.append(String.format(locale, mFormat, toAppendTo.toString()));
        toAppendTo.delete(0, length);
        return toAppendTo;
    }


    /**
     * 显示用户友好的时间
     * <p>
     * 根据传入时间格式为 yyyy-MM-dd HH:mm:ss 的字符串时间，得出是 今天，昨天，星期几，哪个具体时间
     *
     * @param time
     * @return
     */
    public static String formatNiceTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (time == null || "".equals(time)) {
            return "";
        }
        Date operateDate = null;
        try {
            operateDate = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formatNiceTime(operateDate);
    }

    /**
     * 显示用户友好的时间
     * <p>
     * 根据传入date，得出是 今天，昨天，星期几，哪个具体时间
     *
     * @param date
     * @return
     */
    public static String formatNiceTime(Date date) {

        Calendar current = Calendar.getInstance();

        Calendar today = Calendar.getInstance();    //今天
        today.set(Calendar.YEAR, current.get(Calendar.YEAR));
        today.set(Calendar.MONTH, current.get(Calendar.MONTH));
        today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
        //  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        Calendar yesterday = Calendar.getInstance();    //昨天
        yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
        yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
        yesterday.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);

        Calendar oneWeek = Calendar.getInstance();    //一个礼拜以内
        oneWeek.set(Calendar.YEAR, current.get(Calendar.YEAR));
        oneWeek.set(Calendar.MONTH, current.get(Calendar.MONTH));
        oneWeek.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 7);
        oneWeek.set(Calendar.HOUR_OF_DAY, 0);
        oneWeek.set(Calendar.MINUTE, 0);
        oneWeek.set(Calendar.SECOND, 0);


        current.setTime(date);
        if (current.after(today)) {
            return "今天 " + new SimpleDateFormat("HH:mm",locale).format(date);
        } else if (current.before(today) && current.after(yesterday)) {
            return "昨天 " + new SimpleDateFormat("HH:mm",locale).format(date);
        } else if (current.before(yesterday) && current.after(oneWeek)) {
            //一周第一天是否为星期天
            boolean isFirstSunday = (current.getFirstDayOfWeek() == Calendar.SUNDAY);
            //获取周几
            int weekDay = current.get(Calendar.DAY_OF_WEEK);
            //若一周第一天为星期天，则-1，即中国时间
            if (isFirstSunday) {
                weekDay = weekDay - 1;
            }
            String dayPre = "";
            switch (weekDay) {
                case 0:
                    dayPre = "星期天";
                    break;
                case 1:
                    dayPre = "星期一";
                    break;
                case 2:
                    dayPre = "星期二";
                    break;
                case 3:
                    dayPre = "星期三";
                    break;
                case 4:
                    dayPre = "星期四";
                    break;
                case 5:
                    dayPre = "星期五";
                    break;
                case 6:
                    dayPre = "星期六";
                    break;
            }
            return dayPre + " " + new SimpleDateFormat("HH:mm",locale).format(date);
        } else if (current.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            //一年以内
            SimpleDateFormat sdformat = new SimpleDateFormat("MM-dd HH:mm",locale);
            return sdformat.format(date);
        } else {
            //超过一年
            SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm",locale);
            return sdformat.format(date);
        }
    }
}
