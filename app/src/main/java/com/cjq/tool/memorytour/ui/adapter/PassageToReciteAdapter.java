package com.cjq.tool.memorytour.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.RecitablePassage;

import java.util.List;

/**
 * Created by KAT on 2016/11/1.
 */
public class PassageToReciteAdapter extends SectionToReciteAdapter<RecitablePassage> {

    public PassageToReciteAdapter(Context context) {
        super(context);
    }

    public PassageToReciteAdapter(Context context, List<RecitablePassage> sections) {
        super(context, sections);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item_to_recite_passage, null);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView)convertView.findViewById(R.id.tv_passage_name);
            viewHolder.tvState = (TextView)convertView.findViewById(R.id.tv_passage_state);
            viewHolder.chkRecitable = (CheckBox)convertView.findViewById(R.id.chk_to_recite_section);
            viewHolder.onRecitableClickListener = new OnRecitableClickListener();
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        final RecitablePassage passage = getItem(position);
        viewHolder.tvName.setText(passage.getName());
        viewHolder.tvState.setText(passage.getMemoryState().getLabel());
        viewHolder.chkRecitable.setEnabled(passage.isEnableRecite());
        viewHolder.chkRecitable.setChecked(passage.isRecitable());
        viewHolder.onRecitableClickListener.setPosition(position);
        viewHolder.chkRecitable.setOnClickListener(viewHolder.onRecitableClickListener);
        return convertView;
    }

    private class ViewHolder extends BaseViewHolder {
        public TextView tvState;
    }
}
