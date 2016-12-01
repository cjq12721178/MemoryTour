package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.HistoryRecord;

import java.util.Calendar;
import java.util.List;

/**
 * Created by KAT on 2016/11/28.
 */
public class HistoryRecordDialog extends BaseDialog {

    private static final String TAG = "history";
    private List<HistoryRecord> historyRecords;
    private TextView tvHistoryRecord;

    @Override
    protected int getContentLayoutRes() {
        return R.layout.dialog_content_history_record;
    }

    @Override
    protected void onFindView(View content, @Nullable Bundle savedInstanceState) {
        tvHistoryRecord = (TextView)content.findViewById(R.id.tv_history_record);
        setTitle(getString(R.string.tv_title_history_record));
    }

    @Override
    protected void onSetViewData() {
        tvHistoryRecord.setText(getHistoryRecords());
    }

    private String getHistoryRecords() {
        if (historyRecords == null || historyRecords.size() == 0)
            return getString(R.string.tv_no_history_record);
        Calendar calendar = Calendar.getInstance();
        int year = 0;
        StringBuilder builder = new StringBuilder(100);
        for (HistoryRecord record :
                historyRecords) {
            calendar.setTimeInMillis(record.getDate());
            int tmpYear = calendar.get(Calendar.YEAR);
            if (tmpYear != year) {
                if (year != 0) {
                    builder.append('\n');
                }
                year = tmpYear;
                builder.append(year)
                        .append("年\n")
                        .append("  ");
            } else {
                builder.append(", ");
            }
            builder.append(String.format("%d.%d",
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE)));
        }
        return builder.toString();
    }

    @Override
    protected ExitType getExitType() {
        return ExitType.OK;
    }

    public void show(FragmentManager manager, List<HistoryRecord> historyRecords) {
        this.historyRecords = historyRecords;
        super.show(manager, TAG);
    }

    public int show(FragmentTransaction transaction, List<HistoryRecord> historyRecords) {
        this.historyRecords = historyRecords;
        return super.show(transaction, TAG);
    }
}
