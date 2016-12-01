package com.cjq.tool.memorytour.ui.listener;

import android.view.View;

/**
 * Created by KAT on 2016/11/1.
 */
public abstract class OnListItemClickListener implements View.OnClickListener {

    private int position;

    public int getPosition() {
        return position;
    }

    public OnListItemClickListener setPosition(int position) {
        this.position = position;
        return this;
    }
}
