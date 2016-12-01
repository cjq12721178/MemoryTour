package com.cjq.tool.memorytour.bean;

import android.database.Cursor;
import android.text.TextUtils;

import com.cjq.tool.memorytour.exception.NullExpectRecordException;
import com.cjq.tool.memorytour.util.Converter;
import com.cjq.tool.memorytour.util.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by KAT on 2016/7/11.
 */
public class Passage extends BasePassage {

    private String customName;
    private String authorName;
    private String authorDynasty;
    private String content;
    private String comments;
    private String translation;
    private String appreciation;
    private String authorIntroduction;
    private String experience;
    private MemoryState memoryState;
    private MemoryPattern memoryPattern;
    private ExpectRecord expectRecord;
    //unmodifiable
    private List<HistoryRecord> historyRecords;

    public Passage(int id) {
        super(id);
    }

    //没有MemoryException和MemoryRecord
    public static Passage[] buildMultipleWithoutRecord(Cursor cursor) {
        if (cursor == null)
            return null;
        try {
            ColumnIndex indexer = new ColumnIndex(cursor);
            Passage[] passages = new Passage[cursor.getCount()];
            MemoryState[] memoryStates = MemoryState.values();
            MemoryPattern[] memoryPatterns = MemoryPattern.values();
            int passageIndex = 0;
            while (cursor.moveToNext()) {
                passages[passageIndex++] = buildSingleWithoutRecord(cursor, indexer, memoryStates, memoryPatterns);
            }
            return passages;
        } catch (Exception e) {
            Logger.record(e);
        }
        return null;
    }

    //有MemoryException和MemoryRecord
    public static Passage buildSingleWithRecord(Cursor cursor,
                                                 ColumnIndex indexer,
                                                 MemoryState[] states,
                                                 MemoryPattern[] patterns,
                                                 ExpectRecord expectation,
                                                 HistoryRecord[] histories) {
        Passage passage = buildSingleWithoutRecord(cursor, indexer, states, patterns);
        passage.expectRecord = expectation;
        Arrays.sort(histories, new HistoryRecord.SinglePassageInnerComparator());
        passage.historyRecords = Arrays.asList(histories);
        return passage;
    }

    @SuppressWarnings("unchecked")
    private static Passage buildSingleWithoutRecord(Cursor cursor,
                                                    ColumnIndex indexer,
                                                    MemoryState[] states,
                                                    MemoryPattern[] patterns) {
        Passage passage = new Passage(cursor.getInt(indexer.ID));
        passage.name = cursor.getString(indexer.ORIGIN_NAME);
        passage.customName = cursor.getString(indexer.CUSTOM_NAME);
        passage.authorName = cursor.getString(indexer.AUTHOR_NAME);
        passage.authorDynasty = cursor.getString(indexer.AUTHOR_DYNASTY);
        passage.content = cursor.getString(indexer.CONTENT);
        passage.comments = cursor.getString(indexer.COMMENTS);
        passage.translation = cursor.getString(indexer.TRANSLATION);
        passage.appreciation = cursor.getString(indexer.APPRECIATION);
        passage.authorIntroduction = cursor.getString(indexer.AUTHOR_INTRODUCTION);
        passage.experience = cursor.getString(indexer.EXPERIENCE);
        passage.memoryState = cursor.isNull(indexer.MEMORY_STATE)
                ? MemoryState.TO_RECITE : states[cursor.getInt(indexer.MEMORY_STATE)];
        passage.memoryPattern = cursor.isNull(indexer.MEMORY_PATTERN)
                ? MemoryPattern.EBBINGHAUS : patterns[cursor.getInt(indexer.MEMORY_PATTERN)];
        return passage;
    }

    public String getOriginName() {
        return name;
    }

    public String getCustomName() {
        return customName;
    }

    public String getReciteName() {
        if (!TextUtils.isEmpty(customName))
            return customName;
        if (!TextUtils.isEmpty(name))
            return name;
        if (!TextUtils.isEmpty(content))
            return content.substring(0, Math.min(2, content.length()));
        StringBuilder nameBuilder = new StringBuilder(20);
        if (!TextUtils.isEmpty(authorName)) {
            nameBuilder.append(authorName)
                    .append('·');
        }
        if (!TextUtils.isEmpty(authorDynasty)) {
            nameBuilder.append(authorDynasty)
                    .append('·');
        }
        return nameBuilder.append("无题").toString();
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorDynasty() {
        return authorDynasty;
    }

    public String getContent() {
        return content;
    }

    public String getComments() {
        return comments;
    }

    public String getTranslation() {
        return translation;
    }

    public String getAppreciation() {
        return appreciation;
    }

    public String getAuthorIntroduction() {
        return authorIntroduction;
    }

    public String getExperience() {
        return experience;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public MemoryState getMemoryState() {
        return memoryState;
    }

    public void setMemoryState(MemoryState memoryState) {
        this.memoryState = memoryState;
    }

    public boolean isReciting() {
        return memoryState == MemoryState.RECITING;
    }

    public MemoryPattern getMemoryPattern() {
        return memoryPattern;
    }

    public void setMemoryPattern(MemoryPattern memoryPattern) {
        this.memoryPattern = memoryPattern;
    }

    public ExpectRecord getExpectRecord() {
        return expectRecord;
    }

    public List<HistoryRecord> getHistoryRecords() {
        return historyRecords;
    }

    public ExpectRecord generateFirstExpectRecord(MemoryPattern newPattern) {
        return ExpectRecord.makeFirst(newPattern);
    }

    public ExpectRecord generateNextExpectRecord(boolean remembered) {
        if (expectRecord == null)
            throw new NullExpectRecordException("expectRecord为null，无法产生下一个expectRecord");
        return expectRecord.next(remembered);
    }

    public HistoryRecord generateFirstHistoryRecord(MemoryPattern newPattern) {
        return HistoryRecord.makeFirst(newPattern);
    }

    public HistoryRecord generateHistoryRecord(boolean remembered) {
        if (expectRecord == null)
            throw new NullExpectRecordException("expectRecord为null，无法产生historyRecord");
        return HistoryRecord.make(remembered,
                (int)(TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) -
                        TimeUnit.MILLISECONDS.toDays(expectRecord.getDate())),
                memoryPattern);
    }

    public static class Importer extends Section.Importer {

        private Passage tmpPassage;
        //key为chapter id
        private Map<Integer, List<Passage>> passageMap;
        //private StringBuilder builder;
        private Integer chapterId;

        public Map<Integer, List<Passage>> getPassageMap() {
            return passageMap;
        }

        @Override
        public void startDocument() throws SAXException {
            passageMap = new HashMap<>();
            //builder = new StringBuilder(BUILDER_DEFAULT_LENGTH);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals(PASSAGE)) {
                tmpPassage = new Passage(Integer.decode(attributes.getValue(ID)).intValue());
            }
            builder.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            builder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (localName) {
                case ORIGIN_NAME:tmpPassage.name = builder.toString();break;
                case AUTHOR_NAME:tmpPassage.authorName = builder.toString();break;
                case AUTHOR_DYNASTY:tmpPassage.authorDynasty = builder.toString();break;
                case CONTENT:tmpPassage.content = Converter.toSBC(builder);break;
                case COMMENTS:tmpPassage.comments = Converter.toSBC(builder);break;
                case TRANSLATION:tmpPassage.translation = Converter.toSBC(builder);break;
                case APPRECIATION:tmpPassage.appreciation = Converter.toSBC(builder);break;
                case AUTHOR_INTRODUCTION:tmpPassage.authorIntroduction = Converter.toSBC(builder);break;
                case CHAPTER_ID:chapterId = Integer.decode(builder.toString());break;
                case PASSAGE:{
                    if (chapterId != null) {
                        List<Passage> passages = passageMap.get(chapterId);
                        if (passages == null) {
                            passages = new ArrayList<>();
                            passageMap.put(chapterId, passages);
                        }
                        passages.add(tmpPassage);
                    }
                }break;
                default:break;
            }
        }
    }

    public static class ColumnIndex {

        public final int ID;
        public final int ORIGIN_NAME;
        public final int CUSTOM_NAME;
        public final int AUTHOR_NAME;
        public final int AUTHOR_DYNASTY;
        public final int CONTENT;
        public final int COMMENTS;
        public final int TRANSLATION;
        public final int APPRECIATION;
        public final int AUTHOR_INTRODUCTION;
        public final int EXPERIENCE;
        public final int MEMORY_STATE;
        public final int MEMORY_PATTERN;

        public ColumnIndex(Cursor cursor) {
            if (cursor == null) {
                throw new NullPointerException("Passage ColumnIndex new failed");
            }
            ID = cursor.getColumnIndex(Passage.ID);
            ORIGIN_NAME = cursor.getColumnIndex(Passage.ORIGIN_NAME);
            CUSTOM_NAME = cursor.getColumnIndex(Passage.CUSTOM_NAME);
            AUTHOR_NAME = cursor.getColumnIndex(Passage.AUTHOR_NAME);
            AUTHOR_DYNASTY = cursor.getColumnIndex(Passage.AUTHOR_DYNASTY);
            CONTENT = cursor.getColumnIndex(Passage.CONTENT);
            COMMENTS = cursor.getColumnIndex(Passage.COMMENTS);
            TRANSLATION = cursor.getColumnIndex(Passage.TRANSLATION);
            APPRECIATION = cursor.getColumnIndex(Passage.APPRECIATION);
            AUTHOR_INTRODUCTION = cursor.getColumnIndex(Passage.AUTHOR_INTRODUCTION);
            EXPERIENCE = cursor.getColumnIndex(Passage.EXPERIENCE);
            MEMORY_STATE = cursor.getColumnIndex(Passage.MEMORY_STATE);
            MEMORY_PATTERN = cursor.getColumnIndex(Passage.MEMORY_PATTERN);
        }
    }
}
