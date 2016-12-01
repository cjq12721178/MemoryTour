package com.cjq.tool.memorytour.bean;

import com.cjq.tool.memorytour.exception.SectionIdException;
import com.cjq.tool.memorytour.util.Converter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KAT on 2016/7/11.
 */
public class Chapter extends BaseChapter {

    //private String name;
    private String introduction;

    public Chapter(int id) {
        super(id);
    }

    public String getIntroduction() {
        return introduction;
    }

    public static class Importer extends Section.Importer {

        private Chapter tmpChapter;
        //key为book id
        private Map<Integer, List<Chapter>> chapterMap;
        //private StringBuilder builder;
        private Integer bookId;

        //List<Chapter>未经排序
        public Map<Integer, List<Chapter>> getChapterMap() {
            return chapterMap;
        }

        @Override
        public void startDocument() throws SAXException {
            chapterMap = new HashMap<>();
            //builder = new StringBuilder(BUILDER_DEFAULT_LENGTH);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals(CHAPTER)) {
                tmpChapter = new Chapter(Integer.decode(attributes.getValue(ID)).intValue());
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
                case NAME:tmpChapter.name = builder.toString();break;
                case INTRODUCTION:tmpChapter.introduction = Converter.toSBC(builder);break;
                case BOOK_ID:bookId = Integer.decode(builder.toString());break;
                case CHAPTER:{
                    if (bookId != null) {
                        List<Chapter> chapters = chapterMap.get(bookId);
                        if (chapters == null) {
                            chapters = new ArrayList<>();
                            chapterMap.put(bookId, chapters);
                        }
                        chapters.add(tmpChapter);
                    }
                }break;
                default:break;
            }
        }
    }
}
