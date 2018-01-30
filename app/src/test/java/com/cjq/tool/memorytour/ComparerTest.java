package com.cjq.tool.memorytour;

import com.cjq.tool.memorytour.bean.ExpectRecord;
import com.cjq.tool.memorytour.bean.MemoryPattern;
import com.cjq.tool.memorytour.util.Comparer;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    @Test
    public void canGetRecitedPassage() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //ExpectRecord record = ExpectRecord.makeFirst(MemoryPattern.EBBINGHAUS);
        //long actual = MemoryPattern.EBBINGHAUS.getMemoryTime(0);
        long actual = makeMemoryTime(dateFormat);
        //long expect = TimeUnit.DAYS.toMillis(TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) + 1);
        long expect = System.currentTimeMillis();// + TimeUnit.DAYS.toMillis(1);

        //System.out.println("actual: " + dateFormat.format(new Date(actual)));
        System.out.println("expect: " + dateFormat.format(new Date(expect)));
        assertEquals(expect, actual);
    }

    private long makeMemoryTime(SimpleDateFormat dateFormat) {
        Calendar nextDate = Calendar.getInstance();
        System.out.println("current time : " + dateFormat.format(nextDate.getTime()));
        nextDate.add(Calendar.DAY_OF_MONTH, 1);
        System.out.println("add one day : " + dateFormat.format(nextDate.getTime()));
        nextDate.set(Calendar.HOUR_OF_DAY, 0);
        nextDate.set(Calendar.MINUTE, 0);
        nextDate.set(Calendar.SECOND, 0);
        nextDate.set(Calendar.MILLISECOND, 0);
        System.out.println("clear hour : " + dateFormat.format(nextDate.getTime()));
        return nextDate.getTimeInMillis();
    }
}
