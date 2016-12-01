package com.cjq.tool.memorytour.bean;

import com.cjq.tool.memorytour.exception.SectionIdException;

import java.util.List;

/**
 * Created by KAT on 2016/10/28.
 */
public class BaseBook extends Section {

    //数据库表名
    public static final String BOOK = "book";

    public BaseBook(int id) {
        super(id);
    }

    @Override
    public String getTable() {
        return BOOK;
    }

    @Override
    protected void checkId(int id) {
        if (distinguishId(id) != BOOK_ID)
            throw new SectionIdException("当前id（" + id + "）不符合book id格式（0XXXXXXX-XX000000-00000000-00000000）");
    }
}
