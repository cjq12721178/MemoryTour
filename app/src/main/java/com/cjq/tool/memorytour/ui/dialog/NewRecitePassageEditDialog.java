package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
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

/**
 * Created by KAT on 2016/11/3.
 */
public class NewRecitePassageEditDialog extends BaseDialog {

    private static final String TAG = "recite";
    private EditText etCustomName;
    private Spinner spnMemoryPattern;
    private TextView tvMemoryCycle;
    private Passage passage;
    private ArrayAdapter<MemoryPattern> adapter;
    private OnDestroyEditListener onDestroyEditListener;

    private AdapterView.OnItemSelectedListener onMemoryPatternSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            tvMemoryCycle.setText(adapter.getItem(position).getDescription());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Prompter.show(R.string.ppt_no_memory_pattern_selected);
        }
    };

    private ArrayAdapter<MemoryPattern> getAdapter() {
        if (adapter == null) {
            adapter = new ArrayAdapter(getContext(),
                    android.R.layout.simple_spinner_item,
                    MemoryPattern.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        return adapter;
    }

    private String getDefaultCustomName() {
        String name = passage.getCustomName();
        return name == null ? passage.getOriginName() : name;
    }

    private int getDefaultMemoryPatternIndex() {
        MemoryPattern pattern = passage.getMemoryPattern();
        return pattern == null ? 0 : pattern.ordinal();
    }

    public int show(FragmentTransaction transaction, Passage passage) {
        if (passage == null)
            return 0;
        this.passage = passage;
        return super.show(transaction, TAG);
    }

    public void show(FragmentManager manager, Passage passage) {
        if (passage == null)
            return;
        this.passage = passage;
        super.show(manager, TAG);
    }

    public void setOnDestroyEditListener(OnDestroyEditListener l) {
        onDestroyEditListener = l;
    }

    @Override
    protected @LayoutRes int getContentLayoutRes() {
        return R.layout.dialog_content_new_recite_passage_edit;
    }

    @Override
    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {
        etCustomName = (EditText)content.findViewById(R.id.et_name_custom);
        tvMemoryCycle = (TextView)content.findViewById(R.id.tv_memory_cycle);
        spnMemoryPattern = (Spinner)content.findViewById(R.id.spn_memory_pattern);
        spnMemoryPattern.setAdapter(getAdapter());
        spnMemoryPattern.setOnItemSelectedListener(onMemoryPatternSelectedListener);
        setTitle(getString(R.string.tv_title_memory_data_setting));
    }

    @Override
    protected void onSetViewData() {
        etCustomName.setText(getDefaultCustomName());
        spnMemoryPattern.setSelection(getDefaultMemoryPatternIndex());
    }

    @Override
    protected boolean onOkClick() {
        if (onDestroyEditListener != null) {
            onDestroyEditListener.onConfirm(etCustomName.getText().toString(),
                    (MemoryPattern) spnMemoryPattern.getSelectedItem());
        }
        return true;
    }

    public interface OnDestroyEditListener {
        void onConfirm(String customName, MemoryPattern pattern);
    }
}
