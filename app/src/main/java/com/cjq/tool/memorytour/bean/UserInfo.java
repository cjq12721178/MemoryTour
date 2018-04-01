package com.cjq.tool.memorytour.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.TimeUtils;
import android.text.TextUtils;

import com.cjq.tool.memorytour.util.Comparer;
import com.cjq.tool.memorytour.util.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by KAT on 2017/2/3.
 */

public class UserInfo {
    private static final String MEMORY_BOOK_ID = "book_id";
    private static final String MEMORY_BOOK_NAME = "book_name";
    private static final String SCHEDULED_DAILY_MEMORY_COUNT = "daily_memory";
    private static final String LAST_LOG_IN_TIME = "time";
    private static final String INTRADAY_RECITED_COUNT = "new_recited";
    private static final String INTRADAY_NEED_REVIEW_COUNT = "reviewing";
    private static final String INTRADAY_REVIEWED_COUNT = "reviewed";

    private static final int DEFAULT_SCHEDULED_DAILY_MEMORY_COUNT = 1;
    private static final int DEFAULT_MEMORY_BOOK_ID = Section.UNKNOWN_ID;
    private static final String DEFAULT_MEMORY_BOOK_NAME = "综合";

    private static SharedPreferences preferences;
    private static UserInfo userInfo;

    private int bookId;
    private String bookName;
    private int scheduledDailyMemoryCount;
    private long lastLogInTime;
    private int intradayRecitedCount;
    private int intradayNeedReviewCount;
    private int intradayReviewedCount;
    private int totalReviewingCount;
    private int totalNotReciteCount;
    private int totalRecitedCount;

    public static boolean load(Context context, Provider provider) {
        if (userInfo != null)
            return true;
        if (context == null || provider == null || !provider.isPrepared())
            return false;
        try {
            preferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
            if (preferences == null)
                return false;
            if (!preferences.contains(LAST_LOG_IN_TIME)) {
                build();
            }
            initialize(provider);
            return true;
        } catch (Exception e) {
            Logger.record(e);
        }
        return false;
    }

    private static void build() {
        getEditor()
                .putInt(MEMORY_BOOK_ID, DEFAULT_MEMORY_BOOK_ID)
                .putString(MEMORY_BOOK_NAME, DEFAULT_MEMORY_BOOK_NAME)
                //.putLong(LAST_LOG_IN_TIME, System.currentTimeMillis())
                .putInt(SCHEDULED_DAILY_MEMORY_COUNT, DEFAULT_SCHEDULED_DAILY_MEMORY_COUNT)
                //.putInt(INTRADAY_NEED_REVIEW_COUNT, provider.getIntradayNeedReviewPassageCount())
                .commit();
    }

    private static void initialize(Provider provider) {
        //若为当日首次加载用户信息，进行特别设置
        long lastLogInTime = preferences.getLong(LAST_LOG_IN_TIME, 0);
        long currentLogInTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = getEditor();
        if (!Comparer.isSameDay(lastLogInTime, currentLogInTime)) {
            editor.putInt(INTRADAY_RECITED_COUNT, 0)
                    .putInt(INTRADAY_REVIEWED_COUNT, 0)
                    .putInt(INTRADAY_NEED_REVIEW_COUNT, provider.getIntradayNeedReviewPassageCount());
        }
        editor.putLong(LAST_LOG_IN_TIME, currentLogInTime).commit();

        //初始化用户信息
        userInfo = new UserInfo();
        userInfo.bookId = preferences.getInt(MEMORY_BOOK_ID, DEFAULT_MEMORY_BOOK_ID);
        userInfo.bookName = preferences.getString(MEMORY_BOOK_NAME, DEFAULT_MEMORY_BOOK_NAME);
        userInfo.scheduledDailyMemoryCount = preferences.getInt(SCHEDULED_DAILY_MEMORY_COUNT, DEFAULT_SCHEDULED_DAILY_MEMORY_COUNT);
        userInfo.lastLogInTime = currentLogInTime;
        userInfo.intradayRecitedCount = preferences.getInt(INTRADAY_RECITED_COUNT, 0);
        userInfo.intradayNeedReviewCount = preferences.getInt(INTRADAY_NEED_REVIEW_COUNT, 0);
        userInfo.intradayReviewedCount = preferences.getInt(INTRADAY_REVIEWED_COUNT, 0);
        userInfo.totalReviewingCount = provider.getTotalReviewingPassageCount();
        userInfo.totalNotReciteCount = provider.getTotalNotRecitePassageCount();
        userInfo.totalRecitedCount = provider.getTotalRecitedPassageCount();
    }

    public static void reInitialize(Provider provider) {
        if (userInfo == null || provider == null) {
            return;
        }
        getEditor().putLong(LAST_LOG_IN_TIME, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
                .commit();
        initialize(provider);
    }

    private UserInfo() {
    }

    private static void check() {
        if (preferences == null) {
            throw new NullPointerException("UserInfo need to be loaded");
        }
    }

    private static SharedPreferences.Editor getEditor() {
        check();
        return preferences.edit();
    }

    //获取所选背诵书目ID
    public static int getMemoryBookId() {
        check();
        return userInfo.bookId;
    }

    public static String getMemoryBookName() {
        check();
        return userInfo.bookName;
    }

    public static void setMemoryBook(BaseBook book) {
        if (book == null) {
            setMemoryBook(DEFAULT_MEMORY_BOOK_ID, DEFAULT_MEMORY_BOOK_NAME);
        } else {
            setMemoryBook(book.getId(), book.getName());
        }
    }

    public static void setMemoryBook(int bookId, String bookName) {
        check();
        if (Section.distinguishId(bookId) != Section.BOOK_ID || TextUtils.isEmpty(bookName)) {
            getEditor().putInt(MEMORY_BOOK_ID, DEFAULT_MEMORY_BOOK_ID)
                    .putString(MEMORY_BOOK_NAME, DEFAULT_MEMORY_BOOK_NAME)
                    .commit();
            userInfo.bookId = DEFAULT_MEMORY_BOOK_ID;
            userInfo.bookName = DEFAULT_MEMORY_BOOK_NAME;
        } else {
            getEditor().putInt(MEMORY_BOOK_ID, bookId)
                    .putString(MEMORY_BOOK_NAME, bookName)
                    .commit();
            userInfo.bookId = bookId;
            userInfo.bookName = bookName;
        }
    }

    //获取计划每日学习（或记忆）章节数目
    public static int getScheduledDailyMemoryCount() {
        check();
        return userInfo.scheduledDailyMemoryCount;
    }

    public static void setScheduledDailyMemoryCount(int scheduledDailyMemoryCount) {
        check();
        if (scheduledDailyMemoryCount > 0) {
            getEditor().putInt(SCHEDULED_DAILY_MEMORY_COUNT, scheduledDailyMemoryCount).commit();
            userInfo.scheduledDailyMemoryCount = scheduledDailyMemoryCount;
        }
    }

    //获取当日首次登陆时间
    public static long getLastLogInTime() {
        check();
        return userInfo.lastLogInTime;
    }

    //获取当日已学习章节数目
    public static int getIntradayRecitedCount() {
        check();
        return userInfo.intradayRecitedCount;
    }

    //获取当日需学习章节数目
    public static int getIntradayRecitingCount() {
        check();
        return Math.max(0, userInfo.scheduledDailyMemoryCount - userInfo.intradayRecitedCount);
    }

    //当日已学习章节数加一，复习计划中需要复习的章节数目加一
    public static void increaseIntradayRecitedCount() {
        check();
        getEditor().putInt(INTRADAY_RECITED_COUNT, userInfo.intradayRecitedCount + 1)
                .commit();
        ++userInfo.intradayRecitedCount;
        ++userInfo.totalReviewingCount;
        --userInfo.totalNotReciteCount;
    }

    //获取当日总共需复习章节数目
    public static int getIntradayNeedReviewCount() {
        check();
        return userInfo.intradayNeedReviewCount;
    }

    //获取当日已复习章节数目
    public static int getIntradayReviewedCount() {
        check();
        return userInfo.intradayReviewedCount;
    }

    //获取当日还未复习章节数目
    public static int getIntradayReviewingCount() {
        check();
        return userInfo.intradayNeedReviewCount - userInfo.intradayReviewedCount;
    }
    
    //复习完一篇章节之后调用
    //当日已复习章节数加一
    //isCompleted == true，则复习计划中需要复习的章节数目减一，已经完成数加一
    public static void increaseIntradayReviewedCount(boolean isCompleted) {
        check();
        getEditor().putInt(INTRADAY_REVIEWED_COUNT, userInfo.intradayReviewedCount + 1)
                .commit();
        ++userInfo.intradayReviewedCount;
        if (isCompleted) {
            --userInfo.totalReviewingCount;
            ++userInfo.totalRecitedCount;
        }
    }

    //获取复习计划中所有需要复习的章节数目
    public static int getTotalReviewingCount() {
        check();
        return userInfo.totalReviewingCount;
    }

    //获取尚未学习的章节数目
    public static int getTotalNotReciteCount() {
        check();
        return userInfo.totalNotReciteCount;
    }

//    public static boolean updateTotalNotReciteCount(Provider provider) {
//        check();
//        if (provider == null)
//            return false;
//        userInfo.totalNotReciteCount = provider.getTotalNotRecitePassageCount();
//        return true;
//    }

    //获取已经完成整个记忆任务的章节数目
    public static int getTotalRecitedCount() {
        check();
        return userInfo.totalRecitedCount;
    }

    public static abstract class Provider {

        public boolean isPrepared() {
            return true;
        }

        public abstract int getIntradayNeedReviewPassageCount();

        public abstract int getTotalReviewingPassageCount();

        public abstract int getTotalNotRecitePassageCount();

        public abstract int getTotalRecitedPassageCount();
    }
}
