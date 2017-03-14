package com.cjq.tool.memorytour;

import com.cjq.tool.memorytour.util.Comparer;

import org.junit.Test;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by KAT on 2017/3/14.
 */

public class ComparerTest {

    @Test
    public void isSameDay_201703130930_201703140730_false() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2017);
        calendar.set(Calendar.MONTH, 2);
        calendar.set(Calendar.DATE, 13);
        calendar.set(Calendar.HOUR, 9);
        calendar.set(Calendar.MINUTE, 30);
        long t1 = calendar.getTimeInMillis();
        calendar.set(Calendar.DATE, 14);
        calendar.set(Calendar.HOUR, 7);
        calendar.set(Calendar.MINUTE, 30);
        long t2 = calendar.getTimeInMillis();
        assertEquals(false, Comparer.isSameDay(t1, t2));
    }

    @Test
    public void isSameDay_201703130930_currentTime_false() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2017);
        calendar.set(Calendar.MONTH, 2);
        calendar.set(Calendar.DATE, 13);
        calendar.set(Calendar.HOUR, 9);
        calendar.set(Calendar.MINUTE, 30);
        long t1 = calendar.getTimeInMillis();
        long t2 = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2);
        assertEquals(false, Comparer.isSameDay(t1, t2));
    }
}
