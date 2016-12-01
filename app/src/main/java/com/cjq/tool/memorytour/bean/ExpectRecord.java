package com.cjq.tool.memorytour.bean;

import android.database.Cursor;

/**
 * Created by KAT on 2016/11/8.
 */
public class ExpectRecord extends Record {

    public static final String EXPECT = "expect";
    public static final String STAGE = "memory_stage";

    private int stage;

    private ExpectRecord(String id) {
        super(id);
    }

    private ExpectRecord() {
        super();
    }

    public static ExpectRecord makeFirst(MemoryPattern currentPattern) {
        //首次记忆成功后生成的记忆预期
        if (currentPattern == null)
            return null;
        return make(null, currentPattern, 0);
    }

    public ExpectRecord next(boolean remembered) {
        return hasNext(remembered) ? make(getId(), pattern, pattern.getNextStage(stage, remembered)) : null;
    }

    private static ExpectRecord make(String id, MemoryPattern pattern, int currStage) {
        ExpectRecord expectation = id == null ?
                new ExpectRecord() :
                new ExpectRecord(id);
        expectation.pattern = pattern;
        expectation.stage = currStage;
        //date在此处表示下一次的记忆日期
        expectation.date = pattern.getMemoryTime(currStage);
        return expectation;
    }

    @SuppressWarnings("unchecked")
    public static ExpectRecord from(Cursor cursor, MemoryPattern[] patterns, ColumnIndex columnIndex) {
        ExpectRecord expectation = new ExpectRecord(cursor.getString(columnIndex.ID));
        expectation.date = cursor.getLong(columnIndex.DATE);
        expectation.pattern = patterns[cursor.getInt(columnIndex.PATTERN)];
        expectation.stage = cursor.getInt(columnIndex.STAGE);
        return expectation;
    }

    public int getStage() {
        return stage;
    }

    public boolean hasNext(boolean remembered) {
        return pattern.getNextStage(stage, remembered) != MemoryPattern.INVALID_STAGE;
    }

    public long getNextReciteDateInMillis(boolean remembered) {
        return pattern.getMemoryTime(pattern.getNextStage(stage, remembered));
    }

    public static class ColumnIndex {
        public final int ID;
        public final int DATE;
        public final int PATTERN;
        public final int STAGE;
        public final int PASSAGE_ID;

        public ColumnIndex(Cursor cursor) {
            if (cursor == null) {
                throw new NullPointerException("ExpectRecord ColumnIndex new failed");
            }
            ID = cursor.getColumnIndex(ExpectRecord.ID);
            DATE = cursor.getColumnIndex(ExpectRecord.MEMORY_DATE);
            PATTERN = cursor.getColumnIndex(ExpectRecord.MEMORY_PATTERN);
            STAGE = cursor.getColumnIndex(ExpectRecord.STAGE);
            PASSAGE_ID = cursor.getColumnIndex(ExpectRecord.PASSAGE_ID);
        }
    }
}
