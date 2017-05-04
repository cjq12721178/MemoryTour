package com.cjq.tool.memorytour.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.cjq.tool.memorytour.exception.RecitableMemoryStateException;
import com.cjq.tool.memorytour.util.Logger;

/**
 * Created by KAT on 2016/10/28.
 */
public class RecitablePassage
        extends BasePassage
        implements Recitable {

    private boolean recitable;
    private MemoryState memoryState;

    public RecitablePassage(int id) {
        super(id);
    }

    protected RecitablePassage(Parcel in) {
        super(in);
        recitable = in.readByte() != 0;
        memoryState = (MemoryState) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (recitable ? 1 : 0));
        dest.writeSerializable(memoryState);
    }

    public static final Creator<RecitablePassage> CREATOR = new Creator<RecitablePassage>() {
        @Override
        public RecitablePassage createFromParcel(Parcel in) {
            return new RecitablePassage(in);
        }

        @Override
        public RecitablePassage[] newArray(int size) {
            return new RecitablePassage[size];
        }
    };

    @Override
    public boolean isRecitable() {
        return recitable;
    }

    @Override
    public void setRecitable(boolean recitable) {
        if (isEnableRecite()) {
            this.recitable = recitable;
        }
    }

    public boolean isEnableRecite() {
        return memoryState == MemoryState.TO_RECITE ||
                memoryState == MemoryState.RECITED;
    }

    public MemoryState getMemoryState() {
        return memoryState;
    }

    //用于返回可加入待记忆侯选库的章节在其加入之后的MemoryState值
    //建议在调用isEnableRecite方法返回true值后使用
    public MemoryState getRecitableMemoryState() {
        switch (memoryState) {
            case TO_RECITE:return MemoryState.NOT_RECITE;
            case RECITED:return MemoryState.REPEAT_RECITE;
            default:throw new RecitableMemoryStateException();
        }
    }

    public void setMemoryState(MemoryState memoryState) {
        this.memoryState = memoryState;
    }

    public static RecitablePassage[] from(Cursor cursor) {
        if (cursor == null)
            return null;
        try {
            int columnIndexId = cursor.getColumnIndex(RecitablePassage.ID);
            int columnIndexName = cursor.getColumnIndex(RecitablePassage.ORIGIN_NAME);
            int columnIndexState = cursor.getColumnIndex(RecitablePassage.MEMORY_STATE);
            RecitablePassage[] passages = new RecitablePassage[cursor.getCount()];
            int passageIndex = 0;
            MemoryState[] memoryStates = MemoryState.values();
            while (cursor.moveToNext()){
                passages[passageIndex] = new RecitablePassage(cursor.getInt(columnIndexId));
                passages[passageIndex].name = cursor.getString(columnIndexName);
                passages[passageIndex].memoryState = cursor.isNull(columnIndexState)
                        ? MemoryState.TO_RECITE : memoryStates[cursor.getInt(columnIndexState)];
                passages[passageIndex].recitable = passages[passageIndex].memoryState == MemoryState.NOT_RECITE
                        || passages[passageIndex].memoryState == MemoryState.REPEAT_RECITE;
                ++passageIndex;
            }
            return passages;
        } catch (Exception e) {
            Logger.record(e);
        }
        return null;
    }
}
