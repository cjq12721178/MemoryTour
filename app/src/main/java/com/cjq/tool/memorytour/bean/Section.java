package com.cjq.tool.memorytour.bean;

import android.database.Cursor;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by KAT on 2016/9/21.
 */
public abstract class Section implements GeneralColumnName {

    //通用列名
    public static final String NAME = "name";
    public static final String AUTHOR_NAME = "author_name";
    public static final String AUTHOR_DYNASTY = "author_dynasty";
    public static final String INTRODUCTION = "introduction";

    private static final int BUILDER_DEFAULT_LENGTH = 256;
    //无效ID，所有小于0的id
    public static final int INVALID_ID = -1;
    //未定ID，一般用于无特定搜索条件搜索时
    public static final int UNKNOWN_ID = 0;
    //用于标识ID类型，see distinguishId
    public static final int BOOK_ID = 1;
    public static final int CHAPTER_ID = 2;
    public static final int PASSAGE_ID = 3;
    //用于checkId
    protected static final int BOOK_ID_RANGE = 0x7FC00000;
    protected static final int CHAPTER_ID_RANGE = 0x003FF000;
    protected static final int PASSAGE_ID_RANGE = 0x00000FFF;
    //最高位为符号位，弃用；
    //23-31位为book id范围，从1开始
    //13-22位为chapter id范围，从1开始
    //1-12位为passage id范围，从1开始
    //book id格式：   0XXXXXXX-XX000000-00000000-00000000
    //chapter id格式：0BBBBBBB-BBXXXXXX-XXXX0000-00000000
    //passage id格式：0BBBBBBB-BBCCCCCC-CCCCXXXX-XXXXXXXX
    //其中X为0或1，0BBBBBBB-BB为该chapter所属book id，
    //            CCCCCC-CCCC为该passage所属chapter id
    protected final int id;
    protected String name;

    public Section(int id) {
        checkId(id);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract String getTable();

    protected abstract void checkId(int id);

    public static int distinguishId(int id) {
        if (id < 0)
            return INVALID_ID;
        if (id == 0)
            return UNKNOWN_ID;
        if ((id & CHAPTER_ID_RANGE) != 0) {
            if ((id & BOOK_ID_RANGE) != 0) {
                return  (id & PASSAGE_ID_RANGE) != 0 ? PASSAGE_ID : CHAPTER_ID;
            } else {
                return INVALID_ID;
            }
        } else {
            return (id & PASSAGE_ID_RANGE) == 0 ? BOOK_ID : INVALID_ID;
        }
    }

    protected static class Importer extends DefaultHandler {

        protected StringBuilder builder = new StringBuilder(BUILDER_DEFAULT_LENGTH);
    }
}
