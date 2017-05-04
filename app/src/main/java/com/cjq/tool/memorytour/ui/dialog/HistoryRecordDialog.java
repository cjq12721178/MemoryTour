package com.cjq.tool.memorytour.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.cjq.tool.qbox.ui.dialog.BaseDialog;
import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.HistoryRecord;

import java.util.Calendar;
import java.util.List;

/**
 * Created by KAT on 2016/11/28.
 */
public class HistoryRecordDialog extends BaseDialog<HistoryRecordDialog.Decorator> {

    private static final String TAG = "in_tag_history";
    private static final String ARGUMENT_KEY_HISTORY_RECORDS_DESCRIPTION = "in_history_record";

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int onSetContentLayout() {
            return R.layout.dialog_content_history_record;
        }
    }

    public HistoryRecordDialog() {
        super();
        setExitType(EXIT_TYPE_OK);
    }

    private String generateHistoryRecordsDescription(List<HistoryRecord> historyRecords) {
        if (historyRecords == null || historyRecords.size() == 0)
            return null;
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
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DATE)));
        }
        return builder.toString();
    }

    @Override
    protected void onSetContentView(View content, Decorator decorator, @Nullable Bundle bundle) {
        TextView tvHistoryRecord = (TextView)content.findViewById(R.id.tv_history_record);
        String description = getArguments().
                getString(ARGUMENT_KEY_HISTORY_RECORDS_DESCRIPTION);
        if (description != null) {
            tvHistoryRecord.setText(description);
            tvHistoryRecord.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        } else {
            tvHistoryRecord.setText(getString(R.string.tv_no_history_record));
            tvHistoryRecord.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }

    public void setHistoryRecord(List<HistoryRecord> historyRecords) {
        getArguments().putString(ARGUMENT_KEY_HISTORY_RECORDS_DESCRIPTION,
                generateHistoryRecordsDescription(historyRecords));
    }

    public void show(FragmentManager manager, List<HistoryRecord> historyRecords) {
        setHistoryRecord(historyRecords);
        super.show(manager, TAG, R.string.tv_title_history_record);
    }

    public int show(FragmentTransaction transaction, List<HistoryRecord> historyRecords) {
        setHistoryRecord(historyRecords);
        return super.show(transaction, TAG, R.string.tv_title_history_record);
    }
}
