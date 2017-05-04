package com.cjq.tool.memorytour.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.cjq.tool.memorytour.exception.IllegalStageException;

import java.util.Calendar;

/**
 * Created by KAT on 2016/9/20.
 */
public enum MemoryPattern {
    EBBINGHAUS("艾宾浩斯记忆模式") {
        @Override
        protected int[] getTimeIntervals() {
            return new int[]{1,2,4,8,16,32,64,128};
        }
    },
    FIBONACCI("斐波那契记忆模式") {
        @Override
        protected int[] getTimeIntervals() {
            return new int[]{1,1,2,3,5,8,13,21,34,55,89};
        }
    };

    private final String name;
    private final int[] timeIntervals;
    private final String description;
    //private int stage = STAGE_NOT_START;
    //public static final int STAGE_NOT_START = -1;
    //public static final int STAGE_HAS_FINISHED = -2;
    public static final int INVALID_STAGE = -1;
    public static final int INVALID_TIME_INTERVAL = -1;

    MemoryPattern(String name) {
        this.name = name;
        timeIntervals = getTimeIntervals();
        description = buildDescription();
    }

    protected abstract int[] getTimeIntervals();

    private String buildDescription() {
        StringBuilder builder = new StringBuilder(50);
        if (timeIntervals.length > 0) {
            builder.append(timeIntervals[0]);
        }
        for (int i = 1;i < timeIntervals.length;++i) {
            builder.append(',')
                    .append(timeIntervals[i]);
        }
        return builder.toString();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }

    public static String[] names() {
        MemoryPattern[] patterns = MemoryPattern.values();
        String[] names = new String[patterns.length];
        for (int i = 0;i < patterns.length;++i) {
            names[i] = patterns[i].name;
        }
        return names;
    }

    //以下所有stage均为当前stage
    public int getCurrentTimeInterval(int stage) {
        return isStageInvalid(stage) ? INVALID_TIME_INTERVAL : timeIntervals[stage];
    }

    public int getNextStage(int stage, boolean remembered) {
        return isStageInvalid(remembered ? ++stage : stage) ? INVALID_STAGE : stage;
    }

    public boolean isStageInvalid(int stage) {
        return stage < 0 || stage >= timeIntervals.length;
    }

    //当前时间加当前记忆阶段所需时间间隔，舍去天以下时间
    public long getMemoryTime(int stage) {
        if (stage == INVALID_STAGE)
            throw new IllegalStageException();
        Calendar nextDate = Calendar.getInstance();
        nextDate.add(Calendar.DAY_OF_MONTH, getCurrentTimeInterval(stage));
        nextDate.set(Calendar.HOUR, 0);
        nextDate.set(Calendar.MINUTE, 0);
        nextDate.set(Calendar.SECOND, 0);
        nextDate.set(Calendar.MILLISECOND, 0);
        return nextDate.getTimeInMillis();
    }
}
