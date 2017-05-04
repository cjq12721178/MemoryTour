package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.memorytour.R;

/**
 * Created by KAT on 2016/11/14.
 */
public class ExperienceEditDialog extends BaseDialog<ExperienceEditDialog.Decorator> implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "in_tag_experience";
    private static final String ARGUMENT_KEY_MODIFIED_EXPERIENCE = "modified_experience";
    private static final String ARGUMENT_KEY_NEW_EXPERIENCE = "new_experience";
    private Switch swtExperienceMode;
    private EditText etExperience;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        changeExperience();
    }

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int onSetContentLayout() {
            return R.layout.dialog_content_experience_edit;
        }
    }

    public interface OnSetExperienceListener {
        void onSetExperience(String experience,
                             boolean addOrModify);
    }

    private void changeExperience() {
        Bundle arguments = getArguments();
        if (swtExperienceMode.isChecked()) {
            arguments.putString(ARGUMENT_KEY_MODIFIED_EXPERIENCE, etExperience.getText().toString());
            etExperience.setText(arguments.getString(ARGUMENT_KEY_NEW_EXPERIENCE));
        } else {
            arguments.putString(ARGUMENT_KEY_NEW_EXPERIENCE, etExperience.getText().toString());
            etExperience.setText(arguments.getString(ARGUMENT_KEY_MODIFIED_EXPERIENCE));
        }
    }

    @Override
    protected boolean onConfirm() {
        OnSetExperienceListener listener = getListener(OnSetExperienceListener.class);
        if (listener != null) {
            listener.onSetExperience(etExperience.getText().toString(), swtExperienceMode.isChecked());
        }
        return true;
    }

    public void setOldExperience(String oldExperience) {
        getArguments().putString(ARGUMENT_KEY_MODIFIED_EXPERIENCE, oldExperience);
    }

    @Override
    public void show(FragmentManager manager, String oldExperience) {
        setOldExperience(oldExperience);
        super.show(manager, TAG, R.string.set_experience);
    }

    @Override
    public int show(FragmentTransaction transaction, String oldExperience) {
        setOldExperience(oldExperience);
        return super.show(transaction, TAG, R.string.set_experience);
    }

    @Override
    protected void onSetContentView(View content, Decorator decorator, @Nullable Bundle bundle) {
        etExperience = (EditText)content.findViewById(R.id.il_experience);
        etExperience.setHint(R.string.et_hint_add_experience);
        etExperience.setMinLines(5);
        etExperience.setMaxLines(10);
        swtExperienceMode = (Switch)content.findViewById(R.id.swt_set_experience_mode);
        swtExperienceMode.setChecked(true);
        swtExperienceMode.setOnCheckedChangeListener(this);
    }
}
