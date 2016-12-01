package com.cjq.tool.memorytour.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.Section;
import com.cjq.tool.memorytour.bean.Recitable;
import com.cjq.tool.memorytour.ui.listener.OnListItemClickListener;

import java.util.List;

/**
 * Created by KAT on 2016/10/17.
 */
public class SectionToReciteAdapter<E extends Section & Recitable> extends BaseAdapter {

    protected final Context context;
    protected final Drawable selectedItemBackground;
    private List<E> sections;
    private OnSectionClickListener onSectionClickListener;
    private int currentSelectedPosition;

    public SectionToReciteAdapter(Context context) {
        this(context, null);
    }

    public SectionToReciteAdapter(Context context, List<E> sections) {
        this.context = context;
        //selectedItemBackground = context.getResources().getDrawable(R.drawable.ic_section_to_recite_selected);
        selectedItemBackground = ContextCompat.getDrawable(context, R.drawable.ic_section_to_recite_selected);
        setSections(sections);
    }

    public void setSections(List<E> sections) {
        this.sections = sections;
        currentSelectedPosition = -1;
    }

    public void setOnSectionClickListener(OnSectionClickListener l) {
        onSectionClickListener = l;
    }

    public void selectSection(int position) {
        if (position < 0 || position >= getCount())
            return;
        clickSection(position);
    }

    private void clickSection(int currentPosition) {
        if (currentSelectedPosition != currentPosition) {
            currentSelectedPosition = currentPosition;
            notifyDataSetChanged();
        }
        if (onSectionClickListener != null) {
            onSectionClickListener.onLabelClick(getItem(currentSelectedPosition));
        }
    }
    
    public void setAllRecitable(boolean recitable) {
        if (sections != null) {
            for (E section :
                    sections) {
                section.setRecitable(recitable);
            }
        }
    }

    @Override
    public int getCount() {
        return sections != null ? sections.size() : 0;
    }

    @Override
    public E getItem(int position) {
        return sections.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item_to_recite_section, null);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView)convertView.findViewById(R.id.tv_name_section);
            viewHolder.chkRecitable = (CheckBox)convertView.findViewById(R.id.chk_to_recite_section);
            viewHolder.onLabelClickListener = new OnLabelClickListener();
            viewHolder.onRecitableClickListener = new OnRecitableClickListener();
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        final E section = getItem(position);
        viewHolder.tvName.setText(section.getName());
        viewHolder.tvName.setBackground(position == currentSelectedPosition ? selectedItemBackground : null);
        viewHolder.chkRecitable.setChecked(section.isRecitable());
        viewHolder.onLabelClickListener.setPosition(position);
        viewHolder.tvName.setOnClickListener(viewHolder.onLabelClickListener);
        viewHolder.onRecitableClickListener.setPosition(position);
        viewHolder.chkRecitable.setOnClickListener(viewHolder.onRecitableClickListener);
        return convertView;
    }

    protected class BaseViewHolder {
        public TextView tvName;
        public CheckBox chkRecitable;
        public OnRecitableClickListener onRecitableClickListener;
    }

    private class ViewHolder extends BaseViewHolder {
        public OnLabelClickListener onLabelClickListener;
    }

    public interface OnSectionClickListener<E> {
        void onLabelClick(E section);
        void onRecitableClick(E section, boolean isSelected);
    }

    protected class OnLabelClickListener extends OnListItemClickListener {

        @Override
        public void onClick(View v) {
            clickSection(getPosition());
        }
    }

    protected class OnRecitableClickListener extends OnListItemClickListener {

        @Override
        public void onClick(View v) {
            if (!(v instanceof CheckBox))
                return;
            CheckBox chkRecitable = (CheckBox)v;
            E section = getItem(getPosition());
            section.setRecitable(chkRecitable.isChecked());
            if (onSectionClickListener != null) {
                onSectionClickListener.onRecitableClick(section, currentSelectedPosition == getPosition());
            }
        }
    }
}
