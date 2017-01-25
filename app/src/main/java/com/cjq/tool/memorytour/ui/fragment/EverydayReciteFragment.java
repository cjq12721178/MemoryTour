package com.cjq.tool.memorytour.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.dialog.ReciteTestDialog;
import com.cjq.tool.memorytour.ui.layout.PassageView;
import com.cjq.tool.memorytour.ui.toast.Prompter;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by KAT on 2016/8/26.
 */
public class EverydayReciteFragment extends BaseFragment {

    private PassageView pvEverydayRecite;
    private ImageButton ibReciteTest;
    private EverydayReciteAdapter everydayReciteAdapter;
    private ReciteTestDialog reciteTestDialog;

    private PassageView.OnEnableFullscreenListener onEnableFullscreenListener = new PassageView.OnEnableFullscreenListener() {
        @Override
        public void onEnableFullScreen(boolean enable) {
            if (everydayReciteAdapter.getPassageCount() > 0) {
                ibReciteTest.setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
            }
            onNotifyMainActivityListener.onEnablePassageFullScreen(enable);
        }
    };

    private View.OnClickListener onReciteTestClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pvEverydayRecite.clear();
            reciteTestDialog.show(getFragmentManager(), everydayReciteAdapter.currPassage());
        }
    };

    private ReciteTestDialog.OnTestEventListener onTestEventListener = new ReciteTestDialog.OnTestEventListener() {
        @Override
        public void onCheckSourceText() {
            pvEverydayRecite.showCurr();
        }

        @Override
        public boolean onNextPassage(boolean remembered) {
            //清空当前显示
            pvEverydayRecite.clear();
            //新加该章节历史记录
            //修改该章节预期记忆
            FinishDailyMissionTask task = new FinishDailyMissionTask();
            task.execute(everydayReciteAdapter.nextPassage(), remembered);
            //判断是否还有下一个章节等待记忆
            //若有，更新ReciteTestDialog显示内容（指向下一章节）
            //若无，1.发出提示“今日背诵任务全部完成”，
            //　　　2.关闭更新ReciteTestDialog显示内容
            //　　　3.隐藏ibReciteTest
            if (everydayReciteAdapter.getPassageCount() == 0) {
                Prompter.show(R.string.ppt_daily_missions_completed);
                ibReciteTest.setVisibility(View.INVISIBLE);
                return true;
            } else {
                reciteTestDialog.refresh(everydayReciteAdapter.currPassage());
                return false;
            }
        }

        @Override
        public void onCustomNameChanged(String newCustomName) {
            ModifyCustomNameTask task = new ModifyCustomNameTask();
            task.execute(everydayReciteAdapter.currPassage(), newCustomName);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        pvEverydayRecite = (PassageView)inflater.inflate(R.layout.fragment_everyday_recite, null);
        everydayReciteAdapter = new EverydayReciteAdapter();
        pvEverydayRecite.setAdapter(everydayReciteAdapter);
        pvEverydayRecite.setOnEnableFullscreenListener(onEnableFullscreenListener);
        pvEverydayRecite.setFunctionBox(getFragmentManager(), getOnExperienceChangedListener(), true);
        ibReciteTest = (ImageButton)pvEverydayRecite.findViewById(R.id.ib_recite_test);
        ibReciteTest.setOnClickListener(onReciteTestClickListener);
        reciteTestDialog = new ReciteTestDialog();
        reciteTestDialog.setOnTestEventListener(onTestEventListener);
        SearchDailyMemoryMissionTask task = new SearchDailyMemoryMissionTask();
        task.execute();
        return pvEverydayRecite;
    }

    private class SearchDailyMemoryMissionTask
            extends AsyncTask<Void, Passage, Passage[]>
            implements SQLiteManager.OnMissionSearchedListener {

        @Override
        protected Passage[] doInBackground(Void... params) {
            return SQLiteManager.searchDailyMissions(this);
        }

        @Override
        public void onMissionSearched(Passage passage) {
            publishProgress(passage);
        }

        @Override
        protected void onProgressUpdate(Passage... values) {
            if (everydayReciteAdapter.getPassageCount() == 0) {
                everydayReciteAdapter.add(values[0]);
                ibReciteTest.setVisibility(View.VISIBLE);
                ibReciteTest.performClick();
            } else {
                everydayReciteAdapter.add(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Passage[] passages) {
            if (passages == null) {
                Prompter.show(EverydayReciteFragment.this.everydayReciteAdapter.getPassageCount() == 0 ?
                        R.string.ppt_daily_missions_load_failed :
                        R.string.ppt_part_mission_load_failed);
            } else if (passages.length == 0) {
                Prompter.show(R.string.ppt_no_daily_missions);
            }
        }
    }

    private class FinishDailyMissionTask extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            if (params == null ||
                    params.length != 2 ||
                    !(params[0] instanceof Passage) ||
                    !(params[1] instanceof Boolean))
                return false;
            return SQLiteManager.finishDailyMission((Passage) params[0],
                    (boolean) params[1]);
        }
    }

    private class ModifyCustomNameTask extends AsyncTask<Object, Void, Boolean> {

        private String newCustomName;
        private Passage passage;

        @Override
        protected Boolean doInBackground(Object... params) {
            if (params == null || params.length != 2 ||
                    !(params[0] instanceof Passage) ||
                    !(params[1] instanceof String))
                return false;
            passage = (Passage) params[0];
            newCustomName = (String)params[1];
            return SQLiteManager.updateMemorySetting(passage, newCustomName, null);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            reciteTestDialog.enableModifyCustomName();
            if (result) {
                passage.setCustomName(newCustomName);
            } else {
                Prompter.show(R.string.ppt_custom_name_modify_failed);
            }
        }
    }

    private static class EverydayReciteAdapter extends PassageView.Adapter {

        private Queue<Passage> passages = new LinkedList<>();

        @Override
        public Passage currPassage() {
            return passages.peek();
        }

        @Override
        public Passage nextPassage() {
            return passages.poll();
        }

        @Override
        public Passage prevPassage() {
            return null;
        }

        @Override
        public int getPassageCount() {
            return passages.size();
        }

        public void add(Passage passage) {
            passages.add(passage);
        }
    }
}
