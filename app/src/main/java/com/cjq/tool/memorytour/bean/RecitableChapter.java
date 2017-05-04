package com.cjq.tool.memorytour.bean;

import android.database.Cursor;
import android.os.Parcel;

import com.cjq.tool.memorytour.util.Logger;

import java.util.List;

/**
 * Created by KAT on 2016/10/28.
 */
public class RecitableChapter
        extends BaseChapter
        implements Recitable {

    private boolean recitable;
    private List<RecitablePassage> passages;

    public RecitableChapter(int id) {
        super(id);
    }

    protected RecitableChapter(Parcel in) {
        super(in);
        recitable = in.readByte() != 0;
        passages = in.createTypedArrayList(RecitablePassage.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte)(recitable ? 1 : 0));
        dest.writeTypedList(passages);
    }

    public static final Creator<RecitableChapter> CREATOR = new Creator<RecitableChapter>() {
        @Override
        public RecitableChapter createFromParcel(Parcel in) {
            return new RecitableChapter(in);
        }

        @Override
        public RecitableChapter[] newArray(int size) {
            return new RecitableChapter[size];
        }
    };

    @Override
    public boolean isRecitable() {
        return recitable;
    }

    @Override
    public void setRecitable(boolean recitable) {
        this.recitable = recitable;
        if (passages != null) {
            for (RecitablePassage passage :
                    passages) {
                passage.setRecitable(recitable);
            }
        }
    }

    public List<RecitablePassage> getPassages() {
        return passages;
    }

    public void setPassages(List<RecitablePassage> passages) {
        this.passages = passages;
    }

    public static RecitableChapter[] from(Cursor cursor) {
        if (cursor == null)
            return null;
        try {
            int columnIndexId = cursor.getColumnIndex(RecitableChapter.ID);
            int columnIndexName = cursor.getColumnIndex(RecitableChapter.NAME);
            RecitableChapter[] chapters = new RecitableChapter[cursor.getCount()];
            int chapterIndex = 0;
            while (cursor.moveToNext()){
                chapters[chapterIndex] = new RecitableChapter(cursor.getInt(columnIndexId));
                chapters[chapterIndex].name = cursor.getString(columnIndexName);
                ++chapterIndex;
            }
            return chapters;
        } catch (Exception e) {
            Logger.record(e);
        }
        return null;
    }
}
