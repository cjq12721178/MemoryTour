package com.cjq.tool.memorytour.util;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by KAT on 2017/3/13.
 */

public class Comparer {

    private Comparer() {
    }

    public static boolean isSameDay(long t1, long t2) {
        final long interval = TimeUnit.MILLISECONDS.toDays(t1 - t2);
        return interval == 0 && toDays(t1) == toDays(t2);
    }

    private static long toDays(long millisecondsWithoutTimeZone) {
        return TimeUnit.MILLISECONDS.toDays(millisecondsWithoutTimeZone +
                TimeZone.getDefault().getOffset(millisecondsWithoutTimeZone));
    }
}
