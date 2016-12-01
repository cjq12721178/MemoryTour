package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.EditText;

import com.cjq.tool.memorytour.R;

/**
 * Created by KAT on 2016/11/25.
 */
public class EditDialog extends BaseDialog {

    public interface OnTextReceiver {
        void onReceive(String text);
    }

    private static final String TAG = "edit";
    private String text;
    private EditText etText;
    private OnTextReceiver onTextReceiver;

    public void setOnTextReceiver(OnTextReceiver l) {
        onTextReceiver = l;
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.dialog_content_edit;
    }

    @Override
    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {
        etText = (EditText)content.findViewById(R.id.il_text);
    }

    @Override
    protected void onSetViewData() {
        etText.setText(text);
    }

    @Override
    protected boolean onOkClick() {
        if (onTextReceiver != null) {
            onTextReceiver.onReceive(etText.getText().toString());
        }
        return true;
    }

    public void show(FragmentManager manager, String text) {
        this.text = text;
        super.show(manager, TAG);
    }

    public int show(FragmentTransaction transaction, String text) {
        this.text = text;
        return super.show(transaction, TAG);
    }
}
