package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.cjq.tool.memorytour.R;

/**
 * Created by KAT on 2016/11/14.
 */
public class ExperienceEditDialog extends BaseDialog {

    private static final String TAG = "experience";
    private String oldExperience;
    private String newExperience;
    private Switch swtExperienceMode;
    private EditText etExperience;
    private OnSetExperienceListener onSetExperienceListener;

    public void setOnSetExperienceListener(OnSetExperienceListener l) {
        onSetExperienceListener = l;
    }

    public interface OnSetExperienceListener {
        void onSetExperience(String experience, boolean addOrModify);
    }

    private CompoundButton.OnCheckedChangeListener onExperienceSetModeChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            changeExperience(isChecked);
        }
    };

    private void changeExperience(boolean addOrModify) {
        if (addOrModify) {
            oldExperience = etExperience.getText().toString();
            etExperience.setText(newExperience);
        } else {
            newExperience = etExperience.getText().toString();
            etExperience.setText(oldExperience);
        }
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.dialog_content_experience_edit;
    }

    @Override
    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {
        etExperience = (EditText)content.findViewById(R.id.il_experience);
        etExperience.setHint(R.string.et_hint_add_experience);
        etExperience.setMinLines(5);
        etExperience.setMaxLines(10);
        swtExperienceMode = (Switch)content.findViewById(R.id.swt_set_experience_mode);
        swtExperienceMode.setOnCheckedChangeListener(onExperienceSetModeChangedListener);
    }

    @Override
    protected void onSetViewData() {
        //此处的用意是，每次界面打开时默认切换至添加体会，同时保证内容为空
        if (!swtExperienceMode.isChecked()) {
            etExperience.setText(oldExperience);
            swtExperienceMode.setChecked(true);
        }
        etExperience.setText("");
    }

    @Override
    protected boolean onOkClick() {
        if (onSetExperienceListener != null) {
            onSetExperienceListener.onSetExperience(etExperience.getText().toString(), swtExperienceMode.isChecked());
        }
        return true;
    }

    @Override
    public void show(FragmentManager manager, String oldExperience) {
        this.oldExperience = oldExperience;
        super.show(manager, TAG);
    }

    @Override
    public int show(FragmentTransaction transaction, String oldExperience) {
        this.oldExperience = oldExperience;
        return super.show(transaction, TAG);
    }
}
