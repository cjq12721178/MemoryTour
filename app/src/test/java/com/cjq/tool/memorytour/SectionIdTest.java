package com.cjq.tool.memorytour;

import com.cjq.tool.memorytour.bean.ExpectRecord;
import com.cjq.tool.memorytour.bean.MemoryPattern;
import com.cjq.tool.memorytour.bean.Section;
import com.cjq.tool.memorytour.util.IdHelper;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by KAT on 2016/10/15.
 */
public class SectionIdTest {

    //B00
    @Test
    public void is_0x70C00000_book_id_true() {
        assertEquals(Section.BOOK_ID, Section.distinguishId(0x70C00000));
    }

    //-B00
    @Test
    public void is_0x80C00000_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0x80C00000));
    }

    //0C0
    @Test
    public void is_0x0031F000_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0x0031F000));
    }

    //-0C0
    @Test
    public void is_0x9031F000_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0x9031F000));
    }

    //BC0
    @Test
    public void is_0x0131F000_chapter_id_true() {
        assertEquals(Section.CHAPTER_ID, Section.distinguishId(0x0131F000));
    }

    //-BC0
    @Test
    public void is_0xA131F000_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0xA131F000));
    }

    //BCP
    @Test
    public void is_0x0131F010_passage_id_true() {
        assertEquals(Section.PASSAGE_ID, Section.distinguishId(0x0131F010));
    }

    //-BCP
    @Test
    public void is_0xB131F010_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0xB131F010));
    }

    //0CP
    @Test
    public void is_0x0031F010_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0x0031F010));
    }

    //-0CP
    @Test
    public void is_0xC031F010_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0xC031F010));
    }

    //B0P
    @Test
    public void is_0x02000010_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0x02000010));
    }

    //-B0P
    @Test
    public void is_0xD2000010_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0xD2000010));
    }

    //00P
    @Test
    public void is_0x00000010_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0x00000010));
    }

    //-00P
    @Test
    public void is_0xE0000010_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0xE0000010));
    }

    //000
    @Test
    public void is_0x00000000_unknown_id_true() {
        assertEquals(Section.UNKNOWN_ID, Section.distinguishId(0x00000000));
    }

    //-00P
    @Test
    public void is_0x80000000_invalid_id_true() {
        assertEquals(Section.INVALID_ID, Section.distinguishId(0x80000000));
    }

    @Test
    public void testIdHelper() {
        String id = IdHelper.compressedUuid();
        assertNotEquals(null, id);
    }

    @Test
    public void makeMemoryException() {
        ExpectRecord m = ExpectRecord.makeFirst(MemoryPattern.EBBINGHAUS);
        assertNotEquals(null, m);
        //Logger.record(m.getStage());
    }
}
