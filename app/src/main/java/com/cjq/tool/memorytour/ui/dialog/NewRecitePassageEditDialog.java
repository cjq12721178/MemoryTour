package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.MemoryPattern;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.qbox.ui.dialog.BaseDialog;

/**
 * Created by KAT on 2016/11/3.
 */
public class NewRecitePassageEditDialog
        extends BaseDialog<NewRecitePassageEditDialog.Decorator>
        implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "in_tag_new_recite";
    private static final String ARGUMENT_KEY_CUSTOM_NAME = "in_custom_name";
    private static final String ARGUMENT_KEY_SELECTED_PATTERN_INDEX = "in_pattern_index";
    private EditText etCustomName;
    private Spinner spnMemoryPattern;
    private TextView tvMemoryCycle;
    private ArrayAdapter<MemoryPattern> adapter;

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int onSetContentLayout() {
            return R.layout.dialog_content_new_recite_passage_edit;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        tvMemoryCycle.setText(adapter.getItem(position).getDescription());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Prompter.show(R.string.ppt_no_memory_pattern_selected);
    }

//    private AdapterView.OnItemSelectedListener onMemoryPatternSelectedListener = new AdapterView.OnItemSelectedListener() {
//        @Override
//        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//            tvMemoryCycle.setText(adapter.getItem(position).getDescription());
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> parent) {
//            Prompter.show(R.string.ppt_no_memory_pattern_selected);
//        }
//    };

    private ArrayAdapter<MemoryPattern> getAdapter() {
        if (adapter == null) {
            adapter = new ArrayAdapter(getContext(),
                    android.R.layout.simple_spinner_item,
                    MemoryPattern.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        return adapter;
    }

    private String getDefaultCustomName(Passage passage) {
        String name = passage.getCustomName();
        return name == null ? passage.getOriginName() : name;
    }

    private int getDefaultMemoryPatternIndex(Passage passage) {
        MemoryPattern pattern = passage.getMemoryPattern();
        return pattern == null ? 0 : pattern.ordinal();
    }

    public void setPassage(Passage passage) {
        if (passage == null)
            throw new NullPointerException();
        Bundle arguments = getArguments();
        arguments.putString(ARGUMENT_KEY_CUSTOM_NAME, getDefaultCustomName(passage));
        arguments.putInt(ARGUMENT_KEY_SELECTED_PATTERN_INDEX, getDefaultMemoryPatternIndex(passage));
    }

    public int show(FragmentTransaction transaction, Passage passage) {
        setPassage(passage);
        return super.show(transaction, TAG, R.string.tv_title_memory_data_setting);
    }

    public void show(FragmentManager manager, Passage passage) {
        setPassage(passage);
        super.show(manager, TAG, R.string.tv_title_memory_data_setting);
    }

//    public void setOnDestroyEditListener(OnPassageRecitedListener l) {
//        mOnPassageRecitedListener = l;
//    }

//    @Override
//    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {
//        etCustomName = (EditText)content.findViewById(R.id.et_name_custom);
//        tvMemoryCycle = (TextView)content.findViewById(R.id.tv_memory_cycle);
//        spnMemoryPattern = (Spinner)content.findViewById(R.id.spn_memory_pattern);
//        spnMemoryPattern.setAdapter(getAdapter());
//        spnMemoryPattern.setOnItemSelectedListener(onMemoryPatternSelectedListener);
//        setTitle(getString(R.string.tv_title_memory_data_setting));
//    }

//    @Override
//    protected void onSetViewData() {
//        etCustomName.setText(getDefaultCustomName());
//        spnMemoryPattern.setSelection(getDefaultMemoryPatternIndex());
//    }

    @Override
    protected boolean onConfirm() {
        OnPassageRecitedListener listener = getListener(OnPassageRecitedListener.class);
        if (listener != null) {
            listener.onConfirm(etCustomName.getText().toString(),
                    (MemoryPattern) spnMemoryPattern.getSelectedItem());
        }
        return true;
    }

    @Override
    protected void onSetContentView(View content, Decorator decorator, @Nullable Bundle savedInstanceState) {
        etCustomName = (EditText)content.findViewById(R.id.et_name_custom);
        tvMemoryCycle = (TextView)content.findViewById(R.id.tv_memory_cycle);
        spnMemoryPattern = (Spinner)content.findViewById(R.id.spn_memory_pattern);
        spnMemoryPattern.setAdapter(getAdapter());
        spnMemoryPattern.setOnItemSelectedListener(this);
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            etCustomName.setText(arguments.getString(ARGUMENT_KEY_CUSTOM_NAME));
            spnMemoryPattern.setSelection(arguments.getInt(ARGUMENT_KEY_SELECTED_PATTERN_INDEX));
        }
        //setTitle(getString(R.string.tv_title_memory_data_setting));
    }

    public interface OnPassageRecitedListener {
        void onConfirm(String customName, MemoryPattern pattern);
    }
}
