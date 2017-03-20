package com.cjq.tool.memorytour.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.UserInfo;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.Logger;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_REVIEW_STUDY = 1;
    private long exitTime;
    private TextView tvRecitingBook;
    private TextView tvIntradayReciting;
    private TextView tvIntradayReviewing;
    private TextView tvTotalReviewing;
    private TextView tvTotalNotRecite;
    private TextView tvTotalRecited;
    private TouchEventHandler touchEventHandler = new TouchEventHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMainInterface();
        initPrompter();
        registerLogger();
        openDatabase();
        importUserInfo();
    }

    @Override
    protected void onDestroy() {
        closeDatabase();
        super.onDestroy();
    }

    private void setMainInterface() {
        tvRecitingBook = (TextView)findViewById(R.id.tv_reciting_book);
        tvIntradayReciting = (TextView)findViewById(R.id.tv_intraday_reciting);
        tvIntradayReviewing = (TextView)findViewById(R.id.tv_intraday_reviewing);
        tvTotalReviewing = (TextView)findViewById(R.id.tv_total_reviewing);
        tvTotalNotRecite = (TextView)findViewById(R.id.tv_total_not_recite);
        tvTotalRecited = (TextView)findViewById(R.id.tv_total_recited);
        findViewById(R.id.btn_start_memory_tour).setOnClickListener(touchEventHandler);
        findViewById(R.id.tv_library).setOnClickListener(touchEventHandler);
        findViewById(R.id.tv_setting).setOnClickListener(touchEventHandler);
    }

    private void initPrompter() {
        Prompter.init(this);
    }

    private void registerLogger() {
        Logger.register(this);
    }

    private void openDatabase() {
        if (!SQLiteManager.launch(this)) {
            Prompter.show(R.string.ppt_database_open_failed);
        }
    }

    private void importUserInfo() {
        ImportUserInfoTask importUserInfoTask = new ImportUserInfoTask();
        importUserInfoTask.execute();
    }

    private void closeDatabase() {
        SQLiteManager.shutdown();
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - exitTime > 2000) {
            Prompter.show(R.string.ppt_exit_app);
            exitTime = currentTime;
        } else {
            super.onBackPressed();
        }
    }

    private void updateMemoryInformation() {
        tvRecitingBook.setText(String.format(getString(R.string.tv_reciting_book),
                UserInfo.getMemoryBookName()));
        tvIntradayReciting.setText(String.format(getString(R.string.tv_intraday_reciting),
                UserInfo.getIntradayRecitedCount(),
                UserInfo.getScheduledDailyMemoryCount()));
        tvIntradayReviewing.setText(String.format(getString(R.string.tv_intraday_reviewing),
                UserInfo.getIntradayReviewedCount(),
                UserInfo.getIntradayNeedReviewCount()));
        tvTotalReviewing.setText(String.format(getString(R.string.tv_total_reviewing),
                UserInfo.getTotalReviewingCount()));
        tvTotalNotRecite.setText(String.format(getString(R.string.tv_total_not_recite),
                UserInfo.getTotalNotReciteCount()));
        tvTotalRecited.setText(String.format(getString(R.string.tv_total_recited),
                UserInfo.getTotalRecitedCount()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REVIEW_STUDY) {
            updateMemoryInformation();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ImportUserInfoTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return UserInfo.load(MainActivity.this, SQLiteManager.buildUserInfoProvider());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                updateMemoryInformation();
            } else {
                Prompter.show(R.string.ppt_import_user_info_failed);
            }
        }
    }

    private class TouchEventHandler implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start_memory_tour: {
                    startActivityForResult(new Intent(MainActivity.this,
                            PassageReviewStudyActivity.class),
                            REQUEST_CODE_REVIEW_STUDY);
                } break;
                case R.id.tv_library: {
                    //TODO: library
                } break;
                case R.id.tv_setting: {
                    startActivity(new Intent(MainActivity.this,
                            PassageSettingActivity.class));
                } break;
            }
        }
    }
}
