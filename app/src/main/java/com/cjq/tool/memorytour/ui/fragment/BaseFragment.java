package com.cjq.tool.memorytour.ui.fragment;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.cjq.tool.memorytour.ui.toast.Prompter;

/**
 * Created by KAT on 2016/8/26.
 */
public class BaseFragment extends Fragment {

    protected static final int SELECT_PASSAGE_COUNT = 3;
    //当数据库中没有待诵章节时，无需多次搜索数据库
    private boolean enableSearch = true;
    protected OnNotifyMainActivityListener onNotifyMainActivityListener;
    //private PassageView.OnExperienceChangedListener onExperienceChangedListener;

    protected synchronized boolean isSearchEnabled() {
        return enableSearch;
    }

    protected synchronized void setSearchEnabled(boolean enable) {
        enableSearch = enable;
    }

    public void setOnNotifyMainActivityListener(OnNotifyMainActivityListener l) {
        onNotifyMainActivityListener = l;
    }

//    public PassageView.OnExperienceChangedListener getOnExperienceChangedListener() {
//        if (onExperienceChangedListener == null) {
//            onExperienceChangedListener = new PassageView.OnExperienceChangedListener() {
//                @Override
//                public void onExperienceChanged(PassageView passageView, Passage passage, String newExperience) {
//                    if (!newExperience.equals(passage.getExperience())) {
//                        //ModifyExperienceTask task = new ModifyExperienceTask();
//                        //task.execute(passageView, passage, newExperience);
//                    }
//                }
//            };
//        }
//        return onExperienceChangedListener;
//    }

    protected void promptMessage(int msgId) {
        Prompter.show(msgId);
    }

    protected void promptMessage(String msg) {
        Prompter.show(msg);
    }

    public interface OnNotifyMainActivityListener {
        void onEnablePassageFullScreen(boolean enable);
    }

//    private class ModifyExperienceTask extends AsyncTask<Object, Void, Boolean> {
//
//        private PassageView passageView;
//        private Passage passage;
//        private String experience;
//
//        @Override
//        protected Boolean doInBackground(Object... params) {
//            if (params == null || params.length != 3 ||
//                    !(params[0] instanceof PassageView) ||
//                    !(params[1] instanceof Passage) ||
//                    !(params[2] instanceof String))
//                return null;
//            passageView = (PassageView)params[0];
//            return SQLiteManager.updateExperience(passage = (Passage)params[1],
//                    experience = (String)params[2]);
//        }
//
//        @Override
//        protected void onPostExecute(Boolean isSuccess) {
//            if (isSuccess) {
//                passage.setExperience(experience);
//                passageView.showContent(R.id.rdo_experience);
//            } else {
//                Prompter.show(R.string.ppt_experience_update_failed);
//            }
//        }
//    }
}
