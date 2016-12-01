package com.cjq.tool.memorytour.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by KAT on 2016/7/15.
 */
public class TimeFormatter {

    public static String formatYearMonthDay(long time) {
        timeReceiver.setTime(time);
        return dateFormatYearMonthDay.format(timeReceiver);
    }

    public static String formatYearMonthDayHourMinuteSecond(long time) {
        timeReceiver.setTime(time);
        return dfYearMonthDayHourMinuteSecond.format(timeReceiver);
    }

    private static Date timeReceiver = new Date();
    private static SimpleDateFormat dateFormatYearMonthDay = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dfYearMonthDayHourMinuteSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
