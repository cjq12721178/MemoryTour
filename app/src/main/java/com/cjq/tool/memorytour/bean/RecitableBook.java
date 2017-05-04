package com.cjq.tool.memorytour.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.cjq.tool.memorytour.util.Logger;

import java.util.List;

/**
 * Created by KAT on 2016/10/28.
 */
public class RecitableBook
        extends BaseBook
        implements Recitable {

    private boolean recitable;
    private List<RecitableChapter> chapters;

    public RecitableBook(int id) {
        super(id);
    }

    protected RecitableBook(Parcel in) {
        super(in);
        recitable = in.readByte() != 0;
        chapters = in.createTypedArrayList(RecitableChapter.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (recitable ? 1 : 0));
        dest.writeTypedList(chapters);
    }

    public static final Creator<RecitableBook> CREATOR = new Creator<RecitableBook>() {
        @Override
        public RecitableBook createFromParcel(Parcel in) {
            return new RecitableBook(in);
        }

        @Override
        public RecitableBook[] newArray(int size) {
            return new RecitableBook[size];
        }
    };

    @Override
    public boolean isRecitable() {
        return recitable;
    }

    @Override
    public void setRecitable(boolean recitable) {
        this.recitable = recitable;
        if (chapters != null) {
            for (RecitableChapter chapter :
                    chapters) {
                chapter.setRecitable(recitable);
            }
        }
    }

    public List<RecitableChapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<RecitableChapter> chapters) {
        this.chapters = chapters;
    }

    public static RecitableBook[] from(Cursor cursor) {
        if (cursor == null)
            return null;
        try {
            int columnIndexId = cursor.getColumnIndex(RecitableBook.ID);
            int columnIndexName = cursor.getColumnIndex(RecitableBook.NAME);
            RecitableBook[] books = new RecitableBook[cursor.getCount()];
            int bookIndex = 0;
            while (cursor.moveToNext()){
                books[bookIndex] = new RecitableBook(cursor.getInt(columnIndexId));
                books[bookIndex].name = cursor.getString(columnIndexName);
                ++bookIndex;
            }
            return books;
        } catch (Exception e) {
            Logger.record(e);
        }
        return null;
    }
}
