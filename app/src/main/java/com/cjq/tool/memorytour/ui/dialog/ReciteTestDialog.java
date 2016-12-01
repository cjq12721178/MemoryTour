package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.TimeFormatter;

/**
 * Created by KAT on 2016/11/16.
 */
public class ReciteTestDialog extends BaseDialog {

    public interface OnTestEventListener {
        void onCheckSourceText();
        //返回true关闭对话框，false不关闭
        boolean onNextPassage(boolean remembered);
        //在执行完耗时任务后，记得调用enableModifyCustomName使自定义名称修改功能重新启用
        void onCustomNameChanged(String newCustomName);
    }

    private static final String TAG = "test";
    private TextView tvCustomName;
    private TextView tvExpectReciteTime;
    private RadioGroup rgReciteResult;
    private Passage passage;
    //private boolean memoryChipChanged;
    private OnTestEventListener onTestEventListener;
    private EditDialog editDialog;

    private RadioGroup.OnCheckedChangeListener onReciteResultChangedListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            boolean remembered = checkedId == R.id.rdo_remembered;
            if (passage.getExpectRecord().hasNext(remembered)) {
                tvExpectReciteTime.setText(TimeFormatter.formatYearMonthDay(passage.getExpectRecord().getNextReciteDateInMillis(remembered)));
            } else {
                tvExpectReciteTime.setText(R.string.tv_current_recite_mission_completed);
            }
        }
    };

    private View.OnClickListener onCustomNameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.isEnabled()) {
                if (editDialog == null) {
                    editDialog = new EditDialog();
                    editDialog.setTitle(getString(R.string.ppt_title_modify_custom_name));
                    editDialog.setOnTextReceiver(new EditDialog.OnTextReceiver() {
                        @Override
                        public void onReceive(String text) {
                            if (!TextUtils.equals(text, tvCustomName.getText())) {
                                tvCustomName.setText(text);
                                if (onTestEventListener != null) {
                                    tvCustomName.setEnabled(false);
                                    onTestEventListener.onCustomNameChanged(text);
                                }
                            }
                        }
                    });
                }
                editDialog.show(getFragmentManager(), tvCustomName.getText().toString());
            } else {
                Prompter.show(R.string.ppt_custom_name_changing);
            }
        }
    };

    public void enableModifyCustomName() {
        tvCustomName.setEnabled(true);
    }

    public void setOnTestEventListener(OnTestEventListener l) {
        onTestEventListener = l;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.dialog_content_recite_test;
    }

    @Override
    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        tvCustomName = (TextView)content.findViewById(R.id.tv_recite_name_content);
        tvCustomName.setOnClickListener(onCustomNameClickListener);
        tvExpectReciteTime = (TextView)content.findViewById(R.id.tv_expect_recite_time_content);
        rgReciteResult = (RadioGroup)content.findViewById(R.id.rg_recite_result);
        rgReciteResult.setOnCheckedChangeListener(onReciteResultChangedListener);
        setTitle(getString(R.string.tv_title_recite_test));
    }

    @Override
    protected void onSetViewData() {
        tvCustomName.setText(passage.getReciteName());
        if (rgReciteResult.getCheckedRadioButtonId() == R.id.rdo_remembered) {
            onReciteResultChangedListener.onCheckedChanged(rgReciteResult, R.id.rdo_remembered);
        } else {
            rgReciteResult.check(R.id.rdo_remembered);
        }
//        if (memoryChipChanged) {
//            tvCustomName.setText(passage.getReciteName());
//            if (rgReciteResult.getCheckedRadioButtonId() == R.id.rdo_remembered) {
//                onReciteResultChangedListener.onCheckedChanged(rgReciteResult, R.id.rdo_remembered);
//            } else {
//                rgReciteResult.check(R.id.rdo_remembered);
//            }
//        }
    }

    @Override
    protected int getOkLabelRes() {
        return R.string.btn_check_origin;
    }

    @Override
    protected int getCancelLabelRes() {
        return R.string.btn_next_passage;
    }

    @Override
    protected boolean onOkClick() {
        if (onTestEventListener != null) {
            onTestEventListener.onCheckSourceText();
        }
        return true;
    }

    @Override
    protected boolean onCancelClick() {
        return onTestEventListener == null ? true :
                onTestEventListener.onNextPassage(rgReciteResult.getCheckedRadioButtonId() == R.id.rdo_remembered);
    }

    public void show(FragmentManager manager, Passage chip) {
        setPassage(chip);
        super.show(manager, TAG);
    }

    public int show(FragmentTransaction transaction, Passage chip) {
        setPassage(chip);
        return super.show(transaction, TAG);
    }

    public void refresh(Passage chip) {
        setPassage(chip);
        onSetViewData();
    }

    private void setPassage(Passage chip) {
        //memoryChipChanged = passage != chip;
        passage = chip;
    }
}
