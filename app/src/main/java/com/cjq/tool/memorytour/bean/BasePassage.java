package com.cjq.tool.memorytour.bean;

import android.os.Parcel;

import com.cjq.tool.memorytour.exception.SectionIdException;

/**
 * Created by KAT on 2016/10/28.
 */
public abstract class BasePassage extends Section {

    //数据库表名
    public static final String PASSAGE = "passage";
    //数据库字段名
    public static final String ORIGIN_NAME = "origin_name";
    public static final String CUSTOM_NAME = "custom_name";
    public static final String CONTENT = "content";
    public static final String COMMENTS = "comments";
    public static final String TRANSLATION = "translation";
    public static final String APPRECIATION = "appreciation";
    public static final String AUTHOR_INTRODUCTION = "author_introduction";
    public static final String EXPERIENCE = "experience";
    public static final String MEMORY_STATE = "memory_state";
    public static final String CHAPTER_ID = "chapter_id";

    public BasePassage(int id) {
        super(id);
    }

    protected BasePassage(Parcel src) {
        super(src);
    }

    @Override
    public String getTable() {
        return PASSAGE;
    }

    @Override
    protected void checkId(int id) {
        if (distinguishId(id) != PASSAGE_ID)
            throw new SectionIdException("当前id（" + id + "）不符合passage id格式（0BBBBBBB-BBCCCCCC-CCCCXXXX-XXXXXXXX）");
    }

    public int getBookId() {
        return id & BOOK_ID_RANGE;
    }

    public int getChapterId() {
        return id & ~PASSAGE_ID_RANGE;
    }
}
