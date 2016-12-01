package com.cjq.tool.memorytour.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.Passage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KAT on 2016/10/11.
 */
public class SectionViewFragment extends BaseFragment {

    private TextView tvPassageCarrier;
    private RadioGroup rdoGrpContentPanel;
    private StringBuilder contentBuilder = new StringBuilder(256);
    private List<Passage> passages = new ArrayList<>();
    private int currentPassageIndex = -1;
    private int msgNoMorePassage;

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setMsgReachLastPassage(R.string.ppt_reach_last_passage_default);
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_view, null);
        rdoGrpContentPanel = (RadioGroup)view.findViewById(R.id.rdo_grp_content_panel);
        tvPassageCarrier = (TextView)view.findViewById(R.id.tv_passage_carrier);
        return view;
    }

    protected void setMsgReachLastPassage(@IdRes int msgNoMorePassage) {
        this.msgNoMorePassage = msgNoMorePassage;
    }

    protected void nextPassage() {
        if (currentPassageIndex + 1 == passages.size()) {
            promptMessage(msgNoMorePassage);
            return;
        }
        showPassage(passages.get(++currentPassageIndex), rdoGrpContentPanel.getCheckedRadioButtonId());
    }

    private void showPassage(Passage passage, @IdRes int checkedPanelId) {
        if (passage == null) {
            tvPassageCarrier.setText(getString(R.string.ppt_passage_content_null));
            tvPassageCarrier.setGravity(Gravity.CENTER);
        } else {
            switch (checkedPanelId) {
                case R.id.rdo_main_body:setContent(passage.getOriginName(), passage.getAuthorName(), passage.getAuthorDynasty(), passage.getContent());break;
                case R.id.rdo_comments:setContent(passage.getOriginName(), null, null, passage.getComments());break;
                case R.id.rdo_translation:setContent(passage.getOriginName(), null, null, passage.getTranslation());break;
                case R.id.rdo_appreciation:setContent(passage.getOriginName(), null, null, passage.getAppreciation());break;
                case R.id.rdo_author:setContent(passage.getOriginName(), null, null, passage.getAuthorIntroduction());break;
                default:promptMessage(R.string.ppt_content_panel_choose_error);
            }
        }
    }

    private void setContent(String title, String authorName, String authorDynasty, String content) {
        contentBuilder.setLength(0);
        contentBuilder.append('\n')
                .append(TextUtils.isEmpty(title) ? "无题" : title)
                .append('\n')
                .append('\n');
        int titleLength = contentBuilder.length();
        if (!TextUtils.isEmpty(authorDynasty)) {
            contentBuilder.append('[')
                    .append(authorDynasty)
                    .append(']')
                    .append(' ');
        }
        if (!TextUtils.isEmpty(authorName)) {
            contentBuilder.append(authorName)
                    .append('\n')
                    .append('\n');
        }
        contentBuilder.append(content);
        //SpannableString styleText = new SpannableString(contentBuilder);

//        styleText.setSpan(new TextAppearanceSpan(getContext(), R.style.PassageHeader),
//                0, titleLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        styleText.setSpan(new TextAppearanceSpan(getContext(), R.style.PassageContent),
//                titleLength, contentBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        tvPassageCarrier.setText(styleText);
    }

    @WorkerThread
    protected List<Passage> selectPassagesFromDatabase() {
        return null;
    }

    private class SelectPassageTask extends AsyncTask<Void, Void, List<Passage>> {

        @Override
        protected List<Passage> doInBackground(Void... params) {
            return selectPassagesFromDatabase();
        }

        @Override
        protected void onPostExecute(List<Passage> newPassages) {
            if (newPassages != null && !newPassages.isEmpty()) {
                passages.addAll(newPassages);
                if (currentPassageIndex == -1) {
                    nextPassage();
                }
            }
        }
    }
}
