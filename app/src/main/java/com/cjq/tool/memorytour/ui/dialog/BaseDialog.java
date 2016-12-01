package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;

/**
 * Created by KAT on 2016/11/10.
 */
public abstract class BaseDialog extends DialogFragment {

    protected enum ExitType {
        NULL(0),
        OK_CANCEL(R.layout.group_ok_cancel),
        OK(R.layout.group_ok);

        @LayoutRes
        private int resId;

        ExitType(@LayoutRes int resId) {
            this.resId = resId;
        }

        public int getResId() {
            return resId;
        }
    }

    private int interval = 0;
    private String title;

    private View.OnClickListener onOkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onOkClick()) {
                dismiss();
            }
        }
    };

    private View.OnClickListener onCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onCancelClick()) {
                dismiss();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setChildViewVerticalIntervalDp(getResources().getDimensionPixelSize(R.dimen.margin_vertical_small));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout liBase = (LinearLayout)inflater.inflate(R.layout.dialog_base, null);
        //设置内容
        onFindView(inflater.inflate(getContentLayoutRes(), liBase), savedInstanceState);
        //设置标题（可选）
        setTitle((ViewStub) liBase.findViewById(R.id.vs_title_dialog_edit));
        //设置确定/取消按钮及其事件
        ExitType exitType = getExitType();
        if (exitType != ExitType.NULL) {
            View grpOkCancel = inflater.inflate(exitType.getResId(), liBase);
            setExitButton(grpOkCancel, R.id.btn_ok, getOkLabelRes(), onOkClickListener);
            if (exitType == ExitType.OK_CANCEL) {
                setExitButton(grpOkCancel, R.id.btn_cancel, getCancelLabelRes(), onCancelClickListener);
            }
        }
        //设置子控件垂直间距
        setChildViewVerticalInterval(liBase);
        return liBase;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            onSetViewData();
        }
        super.onViewStateRestored(savedInstanceState);
    }

    private void setExitButton(View group, @IdRes int id, @StringRes int label,
                               View.OnClickListener listener) {
        Button btn = (Button)group.findViewById(id);
        btn.setText(label);
        btn.setOnClickListener(listener);
    }

    protected ExitType getExitType() {
        return ExitType.OK_CANCEL;
    }

    @StringRes
    protected int getOkLabelRes() {
        return R.string.ok;
    }

    @StringRes
    protected int getCancelLabelRes() {
        return R.string.cancel;
    }

    private void setChildViewVerticalInterval(LinearLayout liBase) {
        if (interval > 0) {
            LinearLayout.LayoutParams params;
            for (int i = 0, end = liBase.getChildCount() - 1;i < end;++i) {
                params = (LinearLayout.LayoutParams)liBase.getChildAt(i).getLayoutParams();
                params.setMargins(0, 0, 0, interval);
            }
        }
    }

    //注意，设置子view间距大于0时，同一行最好只有一个控件，
    //若有多个，则其高度最好相同（除非其包裹在一个Layout中）。
    //此方法应在show之前调用
    public void setChildViewVerticalIntervalDp(int intervalDp) {
        interval = intervalDp;
    }

    private void setTitle(ViewStub vsTitle) {
        if (!TextUtils.isEmpty(title)) {
            TextView tvTitle = (TextView) vsTitle.inflate();
            tvTitle.setText(title);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected abstract @LayoutRes int getContentLayoutRes();

    //绑定布局中的view，给view设置固定数据（变化数据在此设置将自动恢复原数据）
    protected abstract void onFindView(View content, @Nullable Bundle savedInstanceState);

    //给view设置变动数据
    protected abstract void onSetViewData();

    protected boolean onOkClick() {
        return true;
    }

    protected boolean onCancelClick() {
        return true;
    }
}
