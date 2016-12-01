package com.cjq.tool.memorytour.bean;

import com.cjq.tool.memorytour.exception.SectionIdException;
import com.cjq.tool.memorytour.util.Converter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KAT on 2016/7/11.
 */
public class Book extends BaseBook {

    //private String name;
    private String authorName;
    private String authorDynasty;
    private String introduction;

    public Book(int id) {
        super(id);
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorDynasty() {
        return authorDynasty;
    }

    public String getIntroduction() {
        return introduction;
    }

    public static class Importer extends Section.Importer {

        private Book tmpBook;
        private List<Book> books;
        //private StringBuilder builder;

        public List<Book> getBooks() {
            return books;
        }

        @Override
        public void startDocument() throws SAXException {
            books = new ArrayList<>();
            //builder = new StringBuilder(BUILDER_DEFAULT_LENGTH);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals(BOOK)) {
                tmpBook = new Book(Integer.decode(attributes.getValue(ID)).intValue());
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
                case NAME:tmpBook.name = builder.toString();break;
                case AUTHOR_NAME:tmpBook.authorName = builder.toString();break;
                case AUTHOR_DYNASTY:tmpBook.authorDynasty = builder.toString();break;
                case INTRODUCTION:tmpBook.introduction = Converter.toSBC(builder);break;
                case BOOK:books.add(tmpBook);break;
                default:break;
            }
        }
    }
}
