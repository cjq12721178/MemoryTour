package com.cjq.tool.memorytour;

import android.test.AndroidTestCase;

import com.cjq.tool.memorytour.bean.Book;
import com.cjq.tool.memorytour.bean.MemoryPattern;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;

import junit.framework.Assert;

/**
 * Created by KAT on 2016/9/5.
 */
public class DatabaseTest extends AndroidTestCase {

    public void testCreateDB() {
        SQLiteManager.launch(getContext());
    }

//    public void test_update_no_string_ags() {
//        assertEquals(true, SQLiteManager.updateBook());
//    }
//    public void test_update_packing() {
//        Passage passage = new Passage(0x01001001);
//        //passage.setCustomName("hehe");
//        passage.setMemoryPattern(MemoryPattern.EBBINGHAUS);
//        assertEquals(true, SQLiteManager.updateMemorySetting(passage));
//    }

//    public void test_update_sql() {
//        Passage passage = new Passage(0x00401001);
//        assertEquals(false, SQLiteManager.updateMemorySetting(passage, "hehe", MemoryPattern.EBBINGHAUS));
//    }

//    public void test_invoke_getColumnIndex_before_cursor_moveToNext() {
//        SQLiteManager.query();
//    }
//
//    public void test_containsSection_false() {
//        Book book = new Book(0x00800000);
//        Assert.assertEquals(false, SQLiteManager.containsSection(book, Book.BOOK));
//    }
//
//    public void test_containsSection_true() {
//        Book book = new Book(0x00400000);
//        Assert.assertEquals(true, SQLiteManager.containsSection(book, Book.BOOK));
//    }
}
