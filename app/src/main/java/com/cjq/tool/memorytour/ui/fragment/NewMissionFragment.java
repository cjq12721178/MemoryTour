package com.cjq.tool.memorytour.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.MemoryPattern;
import com.cjq.tool.memorytour.bean.MemoryState;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.dialog.NewRecitePassageEditDialog;
import com.cjq.tool.memorytour.ui.layout.PassageView;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * Created by KAT on 2016/8/26.
 */
public class NewMissionFragment extends BaseFragment {

    private PassageView pvNewMission;
    private ImageButton ibMemorySign;
    private NewMissionAdapter newMissionAdapter;
    private NewRecitePassageEditDialog newRecitePassageEditDialog;

    private PassageView.OnEnableFullscreenListener onEnableFullscreenListener = new PassageView.OnEnableFullscreenListener() {
        @Override
        public void onEnableFullScreen(boolean enable) {
            if (newMissionAdapter.getPassageCount() > 0) {
                ibMemorySign.setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
            }
            onNotifyMainActivityListener.onEnablePassageFullScreen(enable);
        }
    };

    private NewRecitePassageEditDialog.OnDestroyEditListener onDestroyEditListener = new NewRecitePassageEditDialog.OnDestroyEditListener() {
        @Override
        public void onConfirm(String customName, MemoryPattern pattern) {
            Passage passage = newMissionAdapter.currPassage();
            MemoryState state = passage.getMemoryState();
            if (state == MemoryState.RECITING) {
                if (!customName.equals(passage.getCustomName()) || pattern != passage.getMemoryPattern()) {
                    //更改数据库中该节记忆模式或者自定义名称
                    UpdateMemorySettingTask task = new UpdateMemorySettingTask();
                    task.execute(customName, pattern);
                }
            } else if (state == MemoryState.NOT_RECITE || state == MemoryState.REPEAT_RECITE) {
                //更改数据库中该节状态，设置新的记忆模式和新的自定义名称
                //在数据库预期记忆表中新增预期记录
                //在数据库历史记录表中新增历史记录
                FinishNewMissionTask task = new FinishNewMissionTask();
                task.execute(customName, pattern);
            } else {
                Logger.record("NewMissionFragment onDestroyEditListener onConfirm error");
            }
        }
    };

    private PassageView.OnPassageShowListener onPassageShowListener = new PassageView.OnPassageShowListener() {

        @Override
        public void onShowPrev(Passage passage) {
            setMemorySignBackground(passage.isReciting());
        }

        @Override
        public void onShowNext(Passage passage) {
            setMemorySignBackground(passage.isReciting());
            if (passage == newMissionAdapter.getLast()) {
                importNoRecitePassages(passage.getId());
            }
        }
    };

    private void setMemorySignBackground(boolean isReciting) {
        ibMemorySign.setImageResource(isReciting ?
                R.drawable.ic_recited : R.drawable.ic_no_memory);
    }

    private View.OnClickListener onMemorySignedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.isEnabled()) {
                newRecitePassageEditDialog.show(getFragmentManager(), newMissionAdapter.currPassage());
            } else {
                Prompter.show(R.string.ppt_memory_data_not_modified);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        pvNewMission = (PassageView)inflater.inflate(R.layout.fragment_new_mission, null);
        newMissionAdapter = new NewMissionAdapter();
        pvNewMission.setAdapter(newMissionAdapter);
        pvNewMission.setOnPassageShowListener(onPassageShowListener);
        pvNewMission.setEnableSlideSwitch(true);
        pvNewMission.setOnEnableFullscreenListener(onEnableFullscreenListener);
        pvNewMission.setFunctionBox(getFragmentManager(), getOnExperienceChangedListener(), false);
        ibMemorySign = (ImageButton) pvNewMission.findViewById(R.id.ib_memory_tag);
        ibMemorySign.setOnClickListener(onMemorySignedListener);
        newRecitePassageEditDialog = new NewRecitePassageEditDialog();
        newRecitePassageEditDialog.setOnDestroyEditListener(onDestroyEditListener);
        importNoRecitePassages(Passage.UNKNOWN_ID);
        return pvNewMission;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden && newMissionAdapter.getPassageCount() == 0) {
            setSearchEnabled(true);
            importNoRecitePassages(Passage.UNKNOWN_ID);
        }
    }

    private void importNoRecitePassages(int passageIdLowThresh) {
        if (isSearchEnabled()) {
            SelectPassageTask selectPassageTask = new SelectPassageTask();
            selectPassageTask.execute(passageIdLowThresh, SELECT_PASSAGE_COUNT);
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
                newMissionAdapter.addPassages(Arrays.asList(passages));
                //第一次获取数据后，或者之前没有获得数据
                if (newMissionAdapter.getPassageCount() == passages.length) {
                    ibMemorySign.setVisibility(View.VISIBLE);
                    pvNewMission.showNext();
                }
            } else {
                setSearchEnabled(false);
                Prompter.show(R.string.ppt_no_new_mission);
            }
        }
    }

    private class UpdateMemorySettingTask extends AsyncTask<Object, Void, Boolean> {

        private String newCustomName;
        private MemoryPattern newPattern;

        @Override
        protected void onPreExecute() {
            ibMemorySign.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            if (params == null || params.length != 2 ||
                    !(params[0] instanceof String) ||
                    !(params[1] instanceof MemoryPattern))
                return false;
            return onModifyMemoryData(newMissionAdapter.currPassage(),
                    newCustomName = (String)params[0],
                    newPattern = (MemoryPattern)params[1]);
        }

        protected Boolean onModifyMemoryData(Passage passage, String customName, MemoryPattern pattern) {
            return SQLiteManager.updateMemorySetting(passage, customName, pattern);
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            ibMemorySign.setEnabled(true);
            if (isSuccess) {
                Passage passage = newMissionAdapter.currPassage();
                passage.setCustomName(newCustomName);
                passage.setMemoryPattern(newPattern);
                if (passage.getMemoryState() != MemoryState.RECITING) {
                    passage.setMemoryState(MemoryState.RECITING);
                    setMemorySignBackground(true);
                }
            } else {
                Prompter.show(R.string.ppt_memory_data_modify_failed);
            }
        }
    }

    private class FinishNewMissionTask extends UpdateMemorySettingTask {

        @Override
        protected Boolean onModifyMemoryData(Passage passage, String customName, MemoryPattern pattern) {
            return SQLiteManager.finishNewMission(passage, customName, pattern);
        }
    }

    private static class NewMissionAdapter extends PassageView.Adapter {

        private ArrayList<Passage> passages = new ArrayList<>();
        private int currentPassageIndex = -1;

        private Passage get(int position) {
            return position >= 0 && position < passages.size() ?
                    passages.get(currentPassageIndex = position) : null;
        }

        @Override
        public Passage currPassage() {
            return get(currentPassageIndex);
        }

        @Override
        public Passage nextPassage() {
            return get(currentPassageIndex + 1);
        }

        @Override
        public Passage prevPassage() {
            return get(currentPassageIndex - 1);
        }

        public Passage getLast() {
            return passages.isEmpty() ? null : passages.get(passages.size() - 1);
        }

        @Override
        public int getPassageCount() {
            return passages.size();
        }

        public void addPassages(Collection<Passage> newPassages) {
            if (passages == null)
                return;
            passages.addAll(newPassages);
        }
    }
}
