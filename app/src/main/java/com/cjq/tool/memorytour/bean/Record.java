package com.cjq.tool.memorytour.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.cjq.tool.memorytour.util.IdHelper;

/**
 * Created by KAT on 2016/11/8.
 */
public abstract class Record
        implements GeneralColumnName, Parcelable {

    private final String id;
    protected long date;
    protected MemoryPattern pattern;

    protected Record() {
        this(IdHelper.compressedUuid());
    }

    protected Record(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public MemoryPattern getPattern() {
        return pattern;
    }

    protected Record(Parcel src) {
        id = src.readString();
        date = src.readLong();
        pattern = (MemoryPattern) src.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(date);
        dest.writeSerializable(pattern);
    }
}
