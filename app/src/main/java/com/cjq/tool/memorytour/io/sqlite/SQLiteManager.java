package com.cjq.tool.memorytour.io.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.cjq.tool.memorytour.bean.BasePassage;
import com.cjq.tool.memorytour.bean.Book;
import com.cjq.tool.memorytour.bean.Chapter;
import com.cjq.tool.memorytour.bean.ExpectRecord;
import com.cjq.tool.memorytour.bean.MemoryPattern;
import com.cjq.tool.memorytour.bean.HistoryRecord;
import com.cjq.tool.memorytour.bean.MemoryState;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.bean.Section;
import com.cjq.tool.memorytour.bean.RecitableBook;
import com.cjq.tool.memorytour.bean.RecitableChapter;
import com.cjq.tool.memorytour.bean.RecitablePassage;
import com.cjq.tool.memorytour.bean.UserInfo;
import com.cjq.tool.memorytour.util.Logger;

import java.util.List;

/**
 * Created by KAT on 2016/10/9.
 */
public class SQLiteManager {

    private static final String DATABASE_NAME = "MemoryStorage.db";
    private static final int VN_QQ = 1;
    private static final int VN_LONG = 2;
    private static final int CURRENT_VERSION_NO = VN_QQ;
    private static SQLiteLauncher launcher;
    private static SQLiteDatabase database;
    private static StringBuffer buffer = new StringBuffer();
    private static ContentValues contentValues = new ContentValues();

    protected SQLiteManager() {
    }

    public static boolean launch(Context context) {
        if (database != null)
            return true;
        if (context == null)
            return false;
        try {
            if (launcher == null) {
                launcher = new SQLiteLauncher(context, DATABASE_NAME, CURRENT_VERSION_NO);
            }
            database = launcher.getWritableDatabase();
        } catch (Exception e) {
            Logger.record(e);
        }
        return database != null;
    }

    public static void shutdown() {
        if (launcher != null) {
            launcher.close();
            database = null;
            launcher = null;
        }
    }

    private static boolean insertSectionFromXml(Section section) {
        if (database == null || section == null)
            return false;

        try {
            if (database.update(section.getTable(), contentValues, getWhereStatementAsId(Section.ID, section.getId()), null) != 0)
                return true;
            contentValues.put(Section.ID, section.getId());
            return database.insert(section.getTable(), null, contentValues) != -1;
        } catch (Exception e) {
            Logger.record(e);
        }
        return false;
    }

    public static boolean insertBookFromXml(Book book) {
        contentValues.clear();
        contentValues.put(Book.NAME, book.getName());
        contentValues.put(Book.AUTHOR_NAME, book.getAuthorName());
        contentValues.put(Book.AUTHOR_DYNASTY, book.getAuthorDynasty());
        contentValues.put(Book.INTRODUCTION, book.getIntroduction());
        return insertSectionFromXml(book);
    }

    public static boolean insertChapterFromXml(Chapter chapter, Integer bookId) {
        contentValues.clear();
        contentValues.put(Chapter.NAME, chapter.getName());
        contentValues.put(Chapter.INTRODUCTION, chapter.getIntroduction());
        contentValues.put(Chapter.BOOK_ID, bookId);
        return insertSectionFromXml(chapter);
    }

    public static boolean insertPassageFromXml(Passage passage, Integer chapterId) {
        contentValues.clear();
        contentValues.put(Passage.ORIGIN_NAME, passage.getOriginName());
        contentValues.put(Passage.AUTHOR_NAME, passage.getAuthorName());
        contentValues.put(Passage.AUTHOR_DYNASTY, passage.getAuthorDynasty());
        contentValues.put(Passage.CONTENT, passage.getContent());
        contentValues.put(Passage.COMMENTS, passage.getComments());
        contentValues.put(Passage.TRANSLATION, passage.getTranslation());
        contentValues.put(Passage.APPRECIATION, passage.getAppreciation());
        contentValues.put(Passage.CHAPTER_ID, chapterId);
        return insertSectionFromXml(passage);
    }

    @Nullable
    public static Passage[] selectNoRecitePassages(int startPassageId, int expectCount) {
        if (database == null)
            return null;
        int idType = Section.distinguishId(startPassageId);
        if (idType != Section.PASSAGE_ID && idType != Section.UNKNOWN_ID)
            return null;
        buffer.setLength(0);
        buffer.append("SELECT * FROM ")
                .append(Passage.PASSAGE)
                .append(" WHERE ");
        if (idType == Section.PASSAGE_ID) {
            buffer.append(Passage.ID)
                    .append(" > ")
                    .append(startPassageId)
                    .append(" AND ");
        }
        buffer.append("(")
                .append(Passage.MEMORY_STATE)
                .append(" = ")
                .append(MemoryState.NOT_RECITE.ordinal())
                .append(" OR ")
                .append(Passage.MEMORY_STATE)
                .append(" = ")
                .append(MemoryState.REPEAT_RECITE.ordinal())
                .append(')');
        if (expectCount > 0) {
            buffer.append(" LIMIT ")
                    .append(expectCount);
        }
        return Passage.buildMultipleWithoutRecord(query());
    }

    @Nullable
    public static Passage[] searchDailyMissions(OnMissionSearchedListener listener) {
        if (database == null)
            return null;
        try {
            //首先搜索expect表，将今日任务找出
            buffer.setLength(0);
            buffer.append("SELECT * FROM ")
                    .append(ExpectRecord.EXPECT)
                    .append(" WHERE ")
                    .append(ExpectRecord.MEMORY_DATE)
                    .append('<')
                    .append(System.currentTimeMillis())
                    .append(" ORDER BY ")
                    .append(ExpectRecord.MEMORY_DATE)
                    .append(" DESC");
            Cursor cExpects = database.rawQuery(buffer.toString(), null);
            if (cExpects == null)
                return null;
            Passage[] passages = new Passage[cExpects.getCount()];
            int passageIndex = 0;
            ExpectRecord.ColumnIndex ciExpect = new ExpectRecord.ColumnIndex(cExpects);
            Passage.ColumnIndex ciPassage = null;
            HistoryRecord.ColumnIndex ciHistory = null;
            MemoryState[] memoryStates = MemoryState.values();
            MemoryPattern[] memoryPatterns = MemoryPattern.values();
            while (cExpects.moveToNext()) {
                //一个expect对应一个passage，对应一坨history
                ExpectRecord expectRecord = ExpectRecord.from(cExpects, memoryPatterns, ciExpect);
                int passageId = cExpects.getInt(ciExpect.PASSAGE_ID);
                Cursor cHistories = database.rawQuery(getSelectStatementForObject(HistoryRecord.HISTORY, HistoryRecord.PASSAGE_ID, passageId), null);
                if (cHistories == null)
                    return null;
                if (ciHistory == null) {
                    ciHistory = new HistoryRecord.ColumnIndex(cHistories);
                }
                //根据passageId找到相应historyRecords和passage
                HistoryRecord[] historyRecords = HistoryRecord.from(cHistories, memoryPatterns, ciHistory);
                Cursor cPassage = database.rawQuery(getSelectStatementForObject(Passage.PASSAGE, Passage.ID, passageId), null);
                if (cPassage == null || !cPassage.moveToNext())
                    return null;
                if (ciPassage == null) {
                    ciPassage = new Passage.ColumnIndex(cPassage);
                }
                //将expect和historyRecords整合得到带record的passage
                passages[passageIndex] = Passage.buildSingleWithRecord(cPassage, ciPassage, memoryStates, memoryPatterns, expectRecord, historyRecords);
                if (listener != null) {
                    listener.onMissionSearched(passages[passageIndex]);
                }
                ++passageIndex;
            }
            return passages;

        } catch (Exception e) {
            Logger.record(e);
        }
        return null;
    }

    public interface OnMissionSearchedListener {
        void onMissionSearched(Passage passage);
    }

    public static RecitableBook[] importRecitableBooks() {
        if (database == null)
            return null;
        buffer.setLength(0);
        buffer.append("SELECT ")
                .append(RecitableBook.ID)
                .append(',')
                .append(RecitableBook.NAME)
                .append(" FROM ")
                .append(RecitableBook.BOOK)
                .append(" ORDER BY ")
                .append(RecitableBook.ID);
        return RecitableBook.from(query());
    }

    public static RecitableChapter[] importRecitableChapters() {
        if (database == null)
            return null;
        buffer.setLength(0);
        buffer.append("SELECT ")
                .append(RecitableChapter.ID)
                .append(',')
                .append(RecitableChapter.NAME)
                .append(" FROM ")
                .append(RecitableChapter.CHAPTER)
                .append(" ORDER BY ")
                .append(RecitableChapter.BOOK_ID)
                .append(',')
                .append(RecitableChapter.ID);
        return RecitableChapter.from(query());
    }

    public static RecitablePassage[] importRecitablePassages() {
        if (database == null)
            return null;
        buffer.setLength(0);
        buffer.append("SELECT ")
                .append(RecitablePassage.ID)
                .append(',')
                .append(RecitablePassage.ORIGIN_NAME)
                .append(',')
                .append(RecitablePassage.MEMORY_STATE)
                .append(" FROM ")
                .append(RecitablePassage.PASSAGE)
                .append(" ORDER BY ")
                .append(RecitablePassage.CHAPTER_ID)
                .append(',')
                .append(RecitablePassage.ID);
        return RecitablePassage.from(query());
    }

    public static boolean updateRecitablePassages(List<RecitablePassage> passages) {
        if (database == null || passages == null)
            return false;
        database.beginTransaction();
        try {
            for (RecitablePassage passage :
                    passages) {
                if (passage.isEnableRecite() && passage.isRecitable()) {
                    if (!updateMemoryState(passage, passage.getRecitableMemoryState()))
                        return false;
                }
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.record(e);
        } finally {
            database.endTransaction();
        }
        return false;
    }

    private static boolean updateMemoryState(BasePassage passage, MemoryState newState) {
        contentValues.clear();
        contentValues.put(BasePassage.MEMORY_STATE, newState.ordinal());
        return database.update(passage.getTable(), contentValues, getWhereStatementAsId(BasePassage.ID, passage.getId()), null) != 0;
    }

    @SuppressWarnings("unchecked")
    private static boolean setMemorySetting(Passage passage, String newCustomName, MemoryPattern newPattern, boolean first) {
        try {
            contentValues.clear();
            contentValues.put(Passage.CUSTOM_NAME, newCustomName);
            if (newPattern != null) {
                contentValues.put(Passage.MEMORY_PATTERN, newPattern.ordinal());
            }
            if (first) {
                contentValues.put(Passage.MEMORY_STATE, MemoryState.RECITING.ordinal());
            }
            return database.update(passage.getTable(), contentValues, getWhereStatementAsId(Passage.ID, passage.getId()), null) != 0;
        } catch (Exception e) {
            Logger.record(e);
        }
        return false;
    }

    public static boolean updateMemorySetting(Passage passage, String newCustomName, MemoryPattern newPattern) {
        if (database == null || passage == null)
            return false;
        return setMemorySetting(passage, newCustomName, newPattern, false);
    }

    public static boolean finishNewMission(Passage passage, String newCustomName, MemoryPattern newPattern) {
        if (database == null || passage == null || newPattern == null)
            return false;
        database.beginTransaction();
        try {
            //更改该节状态，设置新的记忆模式和新的自定义名称
            if (!setMemorySetting(passage, newCustomName, newPattern, true))
                return false;
            //在预期记忆表中新增预期记录
            if (!updateMemoryExpectation(passage.generateFirstExpectRecord(newPattern), passage.getId()))
                return false;
            //在历史记录表中新增历史记录
            if (!addHistoryMemoryRecord(passage.generateFirstHistoryRecord(newPattern), passage.getId()))
                return false;
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.record(e);
        } finally {
            database.endTransaction();
        }
        return false;
    }

    public static boolean finishDailyMission(Passage passage, boolean remembered) {
        if (database == null || passage == null)
            return false;
        database.beginTransaction();
        try {
            //在历史记录表中新增历史记录
            if (!addHistoryMemoryRecord(passage.generateHistoryRecord(remembered), passage.getId()))
                return false;
            //若本次记忆为此轮最后一次且记忆正确，则删除期记忆表中该passage的预期记录
            //并修改passage的MemoryState为RECITED
            //若不是，则修改预期记忆表中该passage的预期记录
            ExpectRecord nextExpect = passage.generateNextExpectRecord(remembered);
            if (nextExpect == null) {
                if (!deleteMemoryExpectation(passage.getExpectRecord()))
                    return false;
                if (!updateMemoryState(passage, MemoryState.RECITED))
                    return false;
            } else {
                if (!updateMemoryExpectation(nextExpect, passage.getId()))
                    return false;
            }
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.record(e);
        } finally {
            database.endTransaction();
        }
        return false;
    }

    @SuppressWarnings("unchecked/uncaught")
    private static boolean updateMemoryExpectation(ExpectRecord expectation, int passageId) {
        contentValues.clear();
        contentValues.put(ExpectRecord.MEMORY_DATE, expectation.getDate());
        contentValues.put(ExpectRecord.MEMORY_PATTERN, expectation.getPattern().ordinal());
        contentValues.put(ExpectRecord.STAGE, expectation.getStage());
        if (database.update(ExpectRecord.EXPECT, contentValues, getWhereStatementAsId(ExpectRecord.PASSAGE_ID, passageId), null) == 0) {
            contentValues.put(ExpectRecord.ID, expectation.getId());
            contentValues.put(ExpectRecord.PASSAGE_ID, passageId);
            return database.insert(ExpectRecord.EXPECT, null, contentValues) != -1;
        }
        return true;
    }

    @SuppressWarnings("unchecked/uncaught")
    private static boolean deleteMemoryExpectation(ExpectRecord expectRecord) {
        return database.delete(ExpectRecord.EXPECT,
                getWhereStatementAsId(ExpectRecord.ID, expectRecord.getId()),
                null) != 0;
    }

    @SuppressWarnings("unchecked/uncaught")
    private static boolean addHistoryMemoryRecord(HistoryRecord record, int passageId) {
        contentValues.clear();
        contentValues.put(HistoryRecord.ID, record.getId());
        contentValues.put(HistoryRecord.MEMORY_DATE, record.getDate());
        contentValues.put(HistoryRecord.RESULT, record.isRemembered());
        contentValues.put(HistoryRecord.POSTPONE, record.getPostponeDays());
        contentValues.put(HistoryRecord.MEMORY_PATTERN, record.getPattern().ordinal());
        contentValues.put(HistoryRecord.PASSAGE_ID, passageId);
        return database.insert(HistoryRecord.HISTORY, null, contentValues) != -1;
    }

    private static String getWhereStatementAsId(String idName, int idValue) {
        buffer.setLength(0);
        return buffer.append(idName)
                .append('=')
                .append(idValue).toString();
    }

    private static String getWhereStatementAsId(String idName, String idValue) {
        buffer.setLength(0);
        return buffer.append(idName)
                .append("='")
                .append(idValue)
                .append("'")
                .toString();
    }

    private static String getSelectStatementForObject(String table, String idName, int idValue) {
        buffer.setLength(0);
        return buffer.append("SELECT * FROM ")
                .append(table)
                .append(" WHERE ")
                .append(idName)
                .append('=')
                .append(idValue).toString();
    }

    public static boolean updateExperience(Passage passage, String newExperience) {
        if (database == null || passage == null)
            return false;
        try {
            contentValues.clear();
            contentValues.put(Passage.EXPERIENCE, newExperience == null ? "" : newExperience);
            return database.update(passage.getTable(), contentValues, getWhereStatementAsId(Passage.ID, passage.getId()), null) != 0;
        } catch (Exception e) {
            Logger.record(e);
        }
        return false;
    }

    private static Cursor query() {
        try {
            return database.rawQuery(buffer.toString(), null);
        } catch (Exception e) {
            Logger.record(e);
        }
        return null;
    }

    public static UserInfoProvider buildUserInfoProvider() {
        return new UserInfoProvider();
    }

    public static class UserInfoProvider extends UserInfo.Provider {

        private static final String PASSAGE_COUNT = "passage_count";

        @Override
        public boolean isPrepared() {
            return database != null;
        }

        @Override
        public int getIntradayNeedReviewPassageCount() {
            buffer.setLength(0);
            buffer.append("SELECT COUNT(*) AS ")
                    .append(PASSAGE_COUNT)
                    .append(" FROM ")
                    .append(ExpectRecord.EXPECT)
                    .append(" WHERE ")
                    .append(ExpectRecord.MEMORY_DATE)
                    .append('<')
                    .append(System.currentTimeMillis());
            Cursor cursor = database.rawQuery(buffer.toString(), null);
            cursor.moveToNext();
            return cursor.getInt(cursor.getColumnIndex(PASSAGE_COUNT));
        }

        @Override
        public int getTotalReviewingPassageCount() {
            buffer.setLength(0);
            buffer.append("SELECT COUNT(*) AS ")
                    .append(PASSAGE_COUNT)
                    .append(" FROM ")
                    .append(Passage.PASSAGE)
                    .append(" WHERE ")
                    .append(Passage.MEMORY_STATE)
                    .append('=')
                    .append(MemoryState.RECITING.ordinal())
                    .append(" OR ")
                    .append(Passage.MEMORY_STATE)
                    .append('=')
                    .append(MemoryState.REPEAT_RECITE.ordinal());
            Cursor cursor = database.rawQuery(buffer.toString(), null);
            cursor.moveToNext();
            return cursor.getInt(cursor.getColumnIndex(PASSAGE_COUNT));
        }

        @Override
        public int getTotalNotRecitePassageCount() {
            buffer.setLength(0);
            buffer.append("SELECT COUNT(*) AS ")
                    .append(PASSAGE_COUNT)
                    .append(" FROM ")
                    .append(Passage.PASSAGE)
                    .append(" WHERE ")
                    .append(Passage.MEMORY_STATE)
                    .append('=')
                    .append(MemoryState.NOT_RECITE.ordinal());
            Cursor cursor = database.rawQuery(buffer.toString(), null);
            cursor.moveToNext();
            return cursor.getInt(cursor.getColumnIndex(PASSAGE_COUNT));
        }

        @Override
        public int getTotalRecitedPassageCount() {
            buffer.setLength(0);
            buffer.append("SELECT COUNT(*) AS ")
                    .append(PASSAGE_COUNT)
                    .append(" FROM ")
                    .append(Passage.PASSAGE)
                    .append(" WHERE ")
                    .append(Passage.MEMORY_STATE)
                    .append('=')
                    .append(MemoryState.RECITED.ordinal());
            Cursor cursor = database.rawQuery(buffer.toString(), null);
            cursor.moveToNext();
            return cursor.getInt(cursor.getColumnIndex(PASSAGE_COUNT));
        }
    }

    private static class SQLiteLauncher extends SQLiteOpenHelper {

        private StringBuilder sqlBuilder = new StringBuilder(256);

        public SQLiteLauncher(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //创建表“书”
            db.execSQL(getBookTable());
            //创建表“章”
            db.execSQL(getChapterTable());
            //创建表“节”
            db.execSQL(getPassageTable());
            //创建表“历史记录”
            db.execSQL(getHistoryRecordTable());
            //创建表“预期记忆”
            db.execSQL(getExpectMemoryTable());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            if (oldVersion < VN_LONG && newVersion >= VN_LONG) {
//            }
        }

        private String getBookTable() {
            sqlBuilder.setLength(0);
            return sqlBuilder
                    .append("CREATE TABLE book (")
                    .append("id INTEGER PRIMARY KEY,")
                    .append("name VARCHAR(128),")
                    .append("author_name VARCHAR(64),")
                    .append("author_dynasty VARCHAR(32),")
                    .append("introduction TEXT)")
                    .toString();
        }

        private String getChapterTable() {
            sqlBuilder.setLength(0);
            return sqlBuilder
                    .append("CREATE TABLE chapter (")
                    .append("id INTEGER PRIMARY KEY,")
                    .append("name VARCHAR(64),")
                    .append("introduction TEXT,")
                    .append("book_id INTEGER,")
                    .append("FOREIGN KEY(book_id) references book(id))")
                    .toString();
        }

        private String getPassageTable() {
            sqlBuilder.setLength(0);
            return sqlBuilder
                    .append("CREATE TABLE passage (")
                    .append("id INTEGER PRIMARY KEY,")
                    .append("origin_name VARCHAR(64),")
                    .append("custom_name VARCHAR(64),")
                    .append("author_name VARCHAR(64),")
                    .append("author_dynasty VARCHAR(32),")
                    .append("content TEXT,")
                    .append("comments TEXT,")
                    .append("translation TEXT,")
                    .append("appreciation TEXT,")
                    .append("author_introduction TEXT,")
                    .append("experience TEXT,")
                    .append("memory_pattern INTEGER,")
                    .append("memory_state INTEGER,")
                    .append("chapter_id INTEGER,")
                    .append("FOREIGN KEY(chapter_id) references chapter(id))")
                    .toString();
        }

        private String getHistoryRecordTable() {
            sqlBuilder.setLength(0);
            return sqlBuilder
                    .append("CREATE TABLE record (")
                    .append("id NVARCHAR(22) PRIMARY KEY,")
                    .append("memory_date TIMESTAMP,")
                    .append("memory_result BOOLEAN,")
                    .append("postpone INTEGER,")
                    .append("memory_pattern INTEGER,")
                    .append("passage_id INTEGER,")
                    .append("FOREIGN KEY(passage_id) references passage(id))")
                    .toString();
        }

        private String getExpectMemoryTable() {
            sqlBuilder.setLength(0);
            return sqlBuilder
                    .append("CREATE TABLE expect (")
                    .append("id NVARCHAR(22) PRIMARY KEY,")
                    .append("memory_date TIMESTAMP,")
                    .append("memory_pattern INTEGER,")
                    .append("memory_stage INTEGER,")
                    .append("passage_id INTEGER,")
                    .append("FOREIGN KEY(passage_id) references passage(id))")
                    .toString();
        }

//    private String getTableSql(String name,
//                               String primaryKey,
//                               String foreignTableName,
//                               String foreignColumnName,
//                               String... columnNames) {
//        if (name == null || name.length() == 0)
//            throw new SQLException("数据表创建失败，表名不得为空！");
//        if (primaryKey == null || primaryKey.length() == 0)
//            throw new SQLException("数据表创建失败，主键不得为空！");
//        sqlBuilder.setLength(0);
//        sqlBuilder.append("CREATE TABLE ")
//                .append(name)
//                .append(" (")
//                .append(primaryKey)
//                .append(" primary key");
//        if (columnNames != null) {
//            for (String columnName :
//                    columnNames) {
//                sqlBuilder.append(",").append(columnName);
//            }
//        }
//        if (foreignTableName != null &&
//                foreignTableName.length() > 0 &&
//                foreignColumnName != null &&
//                foreignColumnName.length() > 0) {
//            sqlBuilder.append(" REFERENCES ")
//                    .append(foreignTableName)
//                    .append("(")
//                    .append(foreignColumnName)
//                    .append(")");
//        }
//        sqlBuilder.append(")");
//        return sqlBuilder.toString();
//    }
//
//    private static class Field {
//        public String name;
//        public DataType type;
//
//        private static enum DataType {
//            VARCHAR,
//            NVARCHAR,
//            TEXT,
//            INTEGER,
//            FLOAT,
//            BOOLEAN,
//            CLOB,
//            BLOB,
//            TIMESTAMP,
//            NUMERIC,
//
//        }
//    }
    }
}
