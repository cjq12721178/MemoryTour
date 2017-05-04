package com.cjq.tool.memorytour.bean;

import android.os.Parcel;

import com.cjq.tool.memorytour.exception.SectionIdException;

import java.util.List;

/**
 * Created by KAT on 2016/10/28.
 */
public abstract class BaseChapter extends Section {

    //数据库表名
    public static final String CHAPTER = "chapter";
    //数据库字段名
    public static final String BOOK_ID = "book_id";

    public BaseChapter(int id) {
        super(id);
    }

    protected BaseChapter(Parcel in) {
        super(in);
    }

    @Override
    public String getTable() {
        return CHAPTER;
    }

    @Override
    protected void checkId(int id) {
        if (distinguishId(id) != CHAPTER_ID)
            throw new SectionIdException("当前id（" + id + "）不符合chapter id格式（0BBBBBBB-BBXXXXXX-XXXX0000-00000000）");
    }

    public int getBookId() {
        return id & BOOK_ID_RANGE;
    }
}
