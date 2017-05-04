package com.cjq.tool.memorytour.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.cjq.tool.memorytour.exception.NullMemoryPatternException;

import java.util.Comparator;

/**
 * Created by KAT on 2016/11/8.
 */
public class HistoryRecord extends Record {

    public static final String HISTORY = "record";
    public static final String RESULT = "memory_result";
    public static final String POSTPONE = "postpone";

    private boolean result;
    private int postponeDays;

    protected HistoryRecord(Parcel in) {
        super(in);
        result = in.readByte() == 1;
        postponeDays = in.readInt();
    }

    private HistoryRecord() {
        super();
    }

    private HistoryRecord(String id) {
        super(id);
    }

    public static final Parcelable.Creator<HistoryRecord> CREATOR = new Parcelable.Creator<HistoryRecord>() {
        public HistoryRecord createFromParcel(Parcel in) {
            return new HistoryRecord(in);
        }

        public HistoryRecord[] newArray(int size) {
            return new HistoryRecord[size];
        }
    };

    public static HistoryRecord make(boolean remembered,
                                     int postponeDays,
                                     MemoryPattern currentPattern) {
        if (currentPattern == null)
            throw new NullMemoryPatternException("历史记录中不允许MemoryPattern为空");
        HistoryRecord record = new HistoryRecord();
        record.date = System.currentTimeMillis();
        record.result = remembered;
        record.postponeDays = postponeDays;
        record.pattern = currentPattern;
        return record;
    }

    public static HistoryRecord makeFirst(MemoryPattern currentPattern) {
        return make(true, 0, currentPattern);
    }

    //注意，从数据库中取得的历史记录需按日期升序排列
    @SuppressWarnings("unchecked")
    public static HistoryRecord[] from(Cursor cursor, MemoryPattern[] patterns, ColumnIndex columnIndex) {
        HistoryRecord[] histories = new HistoryRecord[cursor.getCount()];
        int historyIndex = 0;
        while (cursor.moveToNext()) {
            HistoryRecord history = new HistoryRecord(cursor.getString(columnIndex.ID));
            history.pattern = patterns[cursor.getInt(columnIndex.PATTERN)];
            history.date = cursor.getLong(columnIndex.DATE);
            history.postponeDays = cursor.getInt(columnIndex.POSTPONE);
            history.result = cursor.getInt(columnIndex.RESULT) == 1;
            histories[historyIndex++] = history;
        }
        return histories;
    }

    public boolean isRemembered() {
        return result;
    }

    public int getPostponeDays() {
        return postponeDays;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte)(result ? 1 : 0));
        dest.writeInt(postponeDays);
    }

    public static class ColumnIndex {

        public final int ID;
        public final int DATE;
        public final int RESULT;
        public final int POSTPONE;
        public final int PATTERN;

        public ColumnIndex(Cursor cursor) {
            if (cursor == null) {
                throw new NullPointerException("HistoryRecord ColumnIndex new failed");
            }
            ID = cursor.getColumnIndex(HistoryRecord.ID);
            DATE = cursor.getColumnIndex(HistoryRecord.MEMORY_DATE);
            RESULT = cursor.getColumnIndex(HistoryRecord.RESULT);
            POSTPONE = cursor.getColumnIndex(HistoryRecord.POSTPONE);
            PATTERN = cursor.getColumnIndex(HistoryRecord.MEMORY_PATTERN);
        }
    }

    public static class SinglePassageInnerComparator implements Comparator<HistoryRecord> {

        @Override
        public int compare(HistoryRecord lhs, HistoryRecord rhs) {
            long result = lhs.date - rhs.date;
            return result < 0 ? -1 : (result > 0 ? 1 : 0);
        }
    }
}
