package com.cjq.tool.memorytour.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageButton;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.MemoryPattern;
import com.cjq.tool.memorytour.bean.MemoryState;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.bean.UserInfo;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.dialog.NewRecitePassageEditDialog;
import com.cjq.tool.memorytour.ui.dialog.ReciteTestDialog;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by KAT on 2017/2/20.
 */

public class PassageReviewStudyActivity
        extends PassageBaseActivity
        implements ReciteTestDialog.OnTestEventListener,
        NewRecitePassageEditDialog.OnPassageRecitedListener {

    private static final String ARGUMENT_KEY_IS_REVIEW_PATTERN = "is_review";
    private static final String ARGUMENT_KEY_IS_REVIEW_DIALOG_PROMPT = "is_review_dialog_prompt";
    private static final String ARGUMENT_KEY_PASSAGES = "passages";

    private ArrayList<Passage> passages = new ArrayList<>();
    private boolean isReviewPattern;
    private ReciteTestDialog reciteTestDialog;
    private NewRecitePassageEditDialog newRecitePassageEditDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDialog(savedInstanceState);
        importPassages(savedInstanceState);
        updatePromptInformation();
    }

    private void initDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            reciteTestDialog = new ReciteTestDialog();
            newRecitePassageEditDialog = new NewRecitePassageEditDialog();
        }
    }

    private void importPassages(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (UserInfo.getIntradayReviewingCount() > 0) {
                //如有章节需要复习，则设置复习模式
//                isReviewPattern = true;
//                setAuxiliaryFunctionImageResource(R.drawable.ic_recite_test);
//                setEnableSlideSwitch(false);
                setPattern(true);
                SearchDailyMemoryMissionTask task = new SearchDailyMemoryMissionTask();
                task.execute();
            } else {
                //设置学习模式
                setPattern(false);
//                isReviewPattern = false;
//                setAuxiliaryFunctionVisibility(View.INVISIBLE);
//                setEnableSlideSwitch(true);
                if (UserInfo.getIntradayRecitingCount() > 0) {
                    if (UserInfo.getIntradayNeedReviewCount() == 0) {
                        Prompter.show(R.string.ppt_no_reviewing_passage);
                    } else {
                        Prompter.show(R.string.ppt_intraday_passages_reviewed);
                    }
                } else {
                    Prompter.show(R.string.ppt_intraday_passages_completed);
                }
                importNoRecitePassages(Passage.UNKNOWN_ID);
            }
        } else {
            isReviewPattern = savedInstanceState.getBoolean(ARGUMENT_KEY_IS_REVIEW_PATTERN);
            passages = savedInstanceState.getParcelableArrayList(ARGUMENT_KEY_PASSAGES);
        }
    }

    private void setPattern(boolean isReview) {
        isReviewPattern = isReview;
        setEnableSlideSwitch(!isReview);
        if (isReview) {
            setAuxiliaryFunctionImageResource(R.drawable.ic_recite_test);
        } else {
            setAuxiliaryFunctionVisibility(View.INVISIBLE);
        }
    }

    private void importNoRecitePassages(int passageIdLowThresh) {
        SelectPassageTask selectPassageTask = new SelectPassageTask();
        selectPassageTask.execute(passageIdLowThresh, SELECT_PASSAGE_COUNT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ARGUMENT_KEY_IS_REVIEW_PATTERN, isReviewPattern);
        outState.putParcelableArrayList(ARGUMENT_KEY_PASSAGES, passages);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            reciteTestDialog = (ReciteTestDialog) getSupportFragmentManager().
                    findFragmentByTag(ReciteTestDialog.TAG);
            newRecitePassageEditDialog = (NewRecitePassageEditDialog) getSupportFragmentManager().
                    findFragmentByTag(NewRecitePassageEditDialog.TAG);
            if (!isReviewPattern || reciteTestDialog == null) {
                restorePassageView();
            }
            if (reciteTestDialog == null) {
                reciteTestDialog = new ReciteTestDialog();
            }
            if (newRecitePassageEditDialog == null) {
                newRecitePassageEditDialog = new NewRecitePassageEditDialog();
            }
            restoreAuxiliaryFunctionButton(savedInstanceState);
        }
    }

    @Override
    protected Passage getPassage(int index) {
        return passages.get(index);
    }

    @Override
    protected int getPassageCount() {
        return passages.size();
    }

    @Override
    protected void onShowPreviousPassage(Passage passage) {
        if (!isReviewPattern) {
            setMemorySignBackground(passage);
        }
    }

    @Override
    protected void onShowNextPassage(Passage passage) {
        if (!isReviewPattern) {
            setMemorySignBackground(passage);
            if (getNextPassage() == null) {
                importNoRecitePassages(passage.getId());
            }
        }
    }

    private void setMemorySignBackground(Passage passage) {
        setAuxiliaryFunctionImageResource(passage.isReciting() ?
                R.drawable.ic_recited : R.drawable.ic_no_memory);
    }

    @Override
    protected void onAuxiliaryFunctionClick(ImageButton ib) {
        if (isReviewPattern) {
            clearPassageView();
            reciteTestDialog.show(getSupportFragmentManager(),
                    getCurrentPassage());
        } else {
            if (ib.isEnabled()) {
                newRecitePassageEditDialog.show(getSupportFragmentManager(),
                        getCurrentPassage());
            } else {
                Prompter.show(R.string.ppt_memory_data_not_modified);
            }
        }
    }

    @Override
    public void onCheckOriginText() {
        showCurrentPassage();
    }

    @Override
    public boolean onNextPassage(boolean remembered) {
        //清空当前显示
        clearPassageView();

        //新加该章节历史记录
        //修改该章节预期记忆
        FinishDailyMissionTask task = new FinishDailyMissionTask();
        task.execute(getCurrentPassage(), remembered);

        //复习任务减一
        UserInfo.increaseIntradayReviewedCount(!getCurrentPassage().getExpectRecord().hasNext(remembered));
        updatePromptInformation();

        //判断是否还有下一个章节等待记忆
        //若有，更新ReciteTestDialog显示内容（指向下一章节）
        //若无，1.发出提示“今日背诵任务全部完成”，
        //　　　2.关闭更新ReciteTestDialog显示内容
        //　　　3.隐藏ibReciteTest
        if (nextPassage() == null) {
            passages.clear();
            setPassageCurrentIndex(-1);
            importPassages(null);
            return true;
        } else {
            reciteTestDialog.refresh(getCurrentPassage());
            return false;
        }
    }

    @Override
    public void onCustomNameChanged(String newCustomName) {
        ModifyCustomNameTask task = new ModifyCustomNameTask();
        task.execute(getCurrentPassage(), newCustomName);
    }

    private void updatePromptInformation() {
        setPromptInformation(String.format(getString(R.string.tv_intraday_review_recite),
                UserInfo.getIntradayReviewingCount(),
                UserInfo.getIntradayRecitingCount()));
    }

    @Override
    public void onConfirm(String customName, MemoryPattern pattern) {
        Passage passage = getCurrentPassage();
        MemoryState state = passage.getMemoryState();
        if (state == MemoryState.RECITING) {
            if (!customName.equals(passage.getCustomName()) || pattern != passage.getMemoryPattern()) {
                //更改数据库中该节记忆模式或者自定义名称
                UpdateMemorySettingTask task = new UpdateMemorySettingTask();
                task.execute(passage, customName, pattern);
            }
        } else if (state == MemoryState.NOT_RECITE || state == MemoryState.REPEAT_RECITE) {
            //更改数据库中该节状态，设置新的记忆模式和新的自定义名称
            //在数据库预期记忆表中新增预期记录
            //在数据库历史记录表中新增历史记录
            FinishNewMissionTask task = new FinishNewMissionTask();
            task.execute(passage, customName, pattern);
        } else {
            Logger.record("NewMissionFragment onDestroyEditListener onConfirm error");
        }
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
            if (getPassageCount() == 0) {
                passages.add(values[0]);
                setPassageCurrentIndex(0);
                setAuxiliaryFunctionVisibility(View.VISIBLE);
                performAuxiliaryFunctionClick();
            } else {
                passages.add(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Passage[] passages) {
            if (passages == null) {
                Prompter.show(getPassageCount() == 0 ?
                        R.string.ppt_daily_missions_load_failed :
                        R.string.ppt_part_mission_load_failed);
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
                reciteTestDialog.refresh(passage);
            } else {
                Prompter.show(R.string.ppt_custom_name_modify_failed);
            }
        }
    }

    private class SelectPassageTask extends AsyncTask<Integer, Void, Passage[]> {

        @Override
        protected Passage[] doInBackground(Integer... params) {
            if (params == null || params.length != 2 ||
                    params[0] == null || params[1] == null)
                return null;
            return SQLiteManager.selectNoRecitePassages(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Passage[] passages) {
            if (passages != null && passages.length > 0) {
                PassageReviewStudyActivity.this.passages.addAll(Arrays.asList(passages));
                //第一次获取数据后，或者之前没有获得数据
                if (getPassageCount() == passages.length) {
                    setAuxiliaryFunctionVisibility(View.VISIBLE);
                    showNextPassage();
                }
            } else {
                Prompter.show(R.string.ppt_no_not_to_recite_passages);
            }
        }
    }

    private class UpdateMemorySettingTask extends AsyncTask<Object, Void, Boolean> {

        protected Passage passage;
        private String newCustomName;
        private MemoryPattern newPattern;

        @Override
        protected void onPreExecute() {
            setAuxiliaryFunctionEnable(false);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            if (params == null || params.length != 3 ||
                    !(params[0] instanceof Passage) ||
                    !(params[1] instanceof String) ||
                    !(params[2] instanceof MemoryPattern))
                return false;
            passage = (Passage)params[0];
            return onModifyMemoryData(newCustomName = (String)params[1],
                    newPattern = (MemoryPattern)params[2]);
        }

        protected Boolean onModifyMemoryData(String customName, MemoryPattern pattern) {
            return SQLiteManager.updateMemorySetting(passage, customName, pattern);
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            setAuxiliaryFunctionEnable(true);
            if (isSuccess) {
                passage.setCustomName(newCustomName);
                passage.setMemoryPattern(newPattern);
                if (passage.getMemoryState() != MemoryState.RECITING) {
                    passage.setMemoryState(MemoryState.RECITING);
                    setMemorySignBackground(passage);
                }
                onPostSuccessExecute();
            } else {
                Prompter.show(R.string.ppt_memory_data_modify_failed);
            }
        }

        protected void onPostSuccessExecute() {

        }
    }

    private class FinishNewMissionTask extends UpdateMemorySettingTask {

        @Override
        protected Boolean onModifyMemoryData(String customName, MemoryPattern pattern) {
            return SQLiteManager.finishNewMission(passage, customName, pattern);
        }

        @Override
        protected void onPostSuccessExecute() {
            UserInfo.increaseIntradayRecitedCount();
            updatePromptInformation();
            if (UserInfo.getIntradayRecitedCount() == UserInfo.getScheduledDailyMemoryCount()) {
                Prompter.show(R.string.ppt_intraday_passages_recited);
            }
        }
    }
}
