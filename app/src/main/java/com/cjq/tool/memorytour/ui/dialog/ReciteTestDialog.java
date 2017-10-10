package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.ExpectRecord;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.TimeFormatter;
import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.qbox.ui.dialog.EditDialog;
import com.cjq.tool.qbox.ui.view.TextViewEx;

/**
 * Created by KAT on 2016/11/16.
 */
public class ReciteTestDialog
        extends BaseDialog<ReciteTestDialog.Decorator>
        implements EditDialog.OnContentReceiver, RadioGroup.OnCheckedChangeListener {

    public static final String TAG = "in_tag_recite_test";
    private static final String ARGUMENT_KEY_PASSAGE_NAME = "in_passage_name";
    private static final String ARGUMENT_KEY_EXPECT_RECORD = "in_expect_record";
    private static final String DIALOG_TAG_RENAME_PASSAGE = "in_rename_passage";
    private TextViewEx tvCustomName;
    private TextView tvExpectReciteTime;
    private RadioGroup rgReciteResult;

    //private Passage passage;
    //private boolean memoryChipChanged;
    //private OnTestEventListener onTestEventListener;
    //private EditDialog editDialog;

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        public void reset() {
            super.reset();
            setOkLabel(R.string.btn_check_origin);
            setCancelLabel(R.string.btn_next_passage);
        }

        @Override
        protected int onSetContentLayout() {
            return R.layout.dialog_content_recite_test;
        }
    }

    @Override
    protected void onSetContentView(View content, Decorator decorator, @Nullable Bundle bundle) {
        setCancelable(false);
        tvCustomName = (TextViewEx) content.findViewById(R.id.tv_recite_name_content);
        tvCustomName.setOnClickListener(this);

        tvExpectReciteTime = (TextView)content.findViewById(R.id.tv_expect_recite_time_content);
        rgReciteResult = (RadioGroup)content.findViewById(R.id.rg_recite_result);
        rgReciteResult.setOnCheckedChangeListener(this);

        setViewValue(false);
    }

    private void setViewValue(boolean includeReciteResultState) {
        Bundle arguments = getArguments();
        tvCustomName.setText(arguments.getString(ARGUMENT_KEY_PASSAGE_NAME));
        if (includeReciteResultState) {
            rgReciteResult.check(R.id.rdo_remembered);
        } else {
            onCheckedChanged(rgReciteResult, rgReciteResult.getCheckedRadioButtonId());
        }
    }

    public interface OnTestEventListener {
        void onCheckOriginText();
        //返回true关闭对话框，false不关闭
        boolean onNextPassage(boolean remembered);
        //在执行完耗时任务后，记得调用enableModifyCustomName使自定义名称修改功能重新启用
        void onCustomNameChanged(String newCustomName);
    }

//    private RadioGroup.OnCheckedChangeListener onReciteResultChangedListener = new RadioGroup.OnCheckedChangeListener() {
//
//        @Override
//        public void onCheckedChanged(RadioGroup group, int checkedId) {
//            boolean remembered = checkedId == R.id.rdo_remembered;
//            if (passage.getExpectRecord().hasNext(remembered)) {
//                tvExpectReciteTime.setText(TimeFormatter.formatYearMonthDay(passage.getExpectRecord().getNextReciteDateInMillis(remembered)));
//            } else {
//                tvExpectReciteTime.setText(R.string.tv_current_recite_mission_completed);
//            }
//        }
//    };

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        boolean remembered = checkedId == R.id.rdo_remembered;
        ExpectRecord expectRecord = getArguments().getParcelable(ARGUMENT_KEY_EXPECT_RECORD);
        if (expectRecord.hasNext(remembered)) {
            tvExpectReciteTime.setText(TimeFormatter.formatYearMonthDay(expectRecord.getNextReciteDateInMillis(remembered)));
        } else {
            tvExpectReciteTime.setText(R.string.tv_current_recite_mission_completed);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.tv_recite_name_content) {
            if (v.isEnabled()) {
                EditDialog dialog = new EditDialog();
                dialog.show(getChildFragmentManager(),
                        DIALOG_TAG_RENAME_PASSAGE,
                        getString(R.string.ppt_title_modify_custom_name),
                        getArguments().getString(ARGUMENT_KEY_PASSAGE_NAME));
            } else {
                Prompter.show(R.string.ppt_custom_name_changing);
            }
        }
    }

    @Override
    public boolean onReceive(EditDialog dialog, String oldValue, String newValue) {
        switch (dialog.getTag()) {
            case DIALOG_TAG_RENAME_PASSAGE:
                if (!TextUtils.equals(oldValue, newValue)) {
                    OnTestEventListener listener = getListener(OnTestEventListener.class);
                    if (listener != null) {
                        tvCustomName.setEnabled(false);
                        listener.onCustomNameChanged(newValue);
                    }
                }
                break;
        }
        return true;
    }

//    private View.OnClickListener onCustomNameClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (v.isEnabled()) {
//                if (editDialog == null) {
//                    editDialog = new EditDialog();
//                    editDialog.setTitle(getString(R.string.ppt_title_modify_custom_name));
//                    editDialog.setOnTextReceiver(new EditDialog.OnTextReceiver() {
//                        @Override
//                        public void onReceive(String text) {
//                            if (!TextUtils.equals(text, tvCustomName.getText())) {
//                                tvCustomName.setText(text);
//                                if (onTestEventListener != null) {
//                                    tvCustomName.setEnabled(false);
//                                    onTestEventListener.onCustomNameChanged(text);
//                                }
//                            }
//                        }
//                    });
//                }
//                editDialog.show(getFragmentManager(), tvCustomName.getText().toString());
//            } else {
//                Prompter.show(R.string.ppt_custom_name_changing);
//            }
//        }
//    };

    public void enableModifyCustomName() {
        tvCustomName.setEnabled(true);
    }

//    public void setOnTestEventListener(OnTestEventListener l) {
//        onTestEventListener = l;
//    }

//    @Override
//    protected void onSetViewData() {
//        tvCustomName.setText(passage.getReciteName());
//        if (rgReciteResult.getCheckedRadioButtonId() == R.id.rdo_remembered) {
//            onReciteResultChangedListener.onCheckedChanged(rgReciteResult, R.id.rdo_remembered);
//        } else {
//            rgReciteResult.check(R.id.rdo_remembered);
//        }
//    }

    @Override
    protected boolean onConfirm() {
        OnTestEventListener listener = getListener(OnTestEventListener.class);
        if (listener != null) {
            listener.onCheckOriginText();
        }
        return true;
    }

    @Override
    protected boolean onCancel() {
        OnTestEventListener listener = getListener(OnTestEventListener.class);
        if (listener == null)
            return true;
        return listener.onNextPassage(rgReciteResult.getCheckedRadioButtonId() == R.id.rdo_remembered);
    }

    public void show(FragmentManager manager, Passage passage) {
        setPassage(passage);
        super.show(manager, TAG, R.string.tv_title_recite_test);
    }

    public int show(FragmentTransaction transaction, Passage passage) {
        setPassage(passage);
        return super.show(transaction, TAG, R.string.tv_title_recite_test);
    }

    public void refresh(Passage chip) {
        setPassage(chip);
        setViewValue(rgReciteResult.getCheckedRadioButtonId() != R.id.rdo_remembered);
    }

    private void setPassage(Passage passage) {
        if (passage == null)
            throw new NullPointerException();
        Bundle arguments = getArguments();
        arguments.putString(ARGUMENT_KEY_PASSAGE_NAME, passage.getCustomName());
        arguments.putParcelable(ARGUMENT_KEY_EXPECT_RECORD, passage.getExpectRecord());
    }
}
