package com.cjq.tool.memorytour.ui.layout;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.ui.dialog.ExperienceEditDialog;
import com.cjq.tool.memorytour.ui.dialog.HistoryRecordDialog;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.Converter;
import com.cjq.tool.memorytour.util.Logger;

/**
 * Created by KAT on 2016/10/17.
 */
public class PassageView extends RelativeLayout {

    public interface OnEnableFullscreenListener {
        void onEnableFullScreen(boolean enable);
    }

    private TextView tvPassageTitle;
    private TextView tvPassageContent;
    private RadioGroup rgContentPanel;
    private ScrollView svPassageCarrier;
    private ImageButton ibSetExperience;
    private ImageButton ibHistoryRecord;
    private StringBuilder contentBuilder = new StringBuilder(256);
    private Adapter adapter;
    private OnPassageShowListener onPassageShowListener;
    private boolean enableSlideSwitch = false;
    private OnEnableFullscreenListener onEnableFullscreenListener;
    private FragmentManager fragmentManager;
    private ExperienceEditDialog experienceEditDialog;
    private OnExperienceChangedListener onExperienceChangedListener;
    private HistoryRecordDialog historyRecordDialog;

    private View.OnClickListener onFunctionPanelClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final Passage passage = adapter.currPassage();
            if (passage == null) {
                Prompter.show(R.string.ppt_passage_content_null);
            } else {
                if (fragmentManager != null) {
                    switch (v.getId()) {
                        case R.id.ib_set_experience:onExperienceEditorClick(passage);break;
                        case R.id.ib_history_record:onHistoryRecordViewerClick(passage);break;
                        default:
                            Logger.record("未点中任何功能按钮");break;
                    }
                }
            }
        }

        private void onExperienceEditorClick(final Passage passage) {
            if (experienceEditDialog == null) {
                experienceEditDialog = new ExperienceEditDialog();
                experienceEditDialog.setOnSetExperienceListener(new ExperienceEditDialog.OnSetExperienceListener() {
                    @Override
                    public void onSetExperience(String newExperience, boolean addOrModify) {
                        if (onExperienceChangedListener != null) {
                            onExperienceChangedListener.onExperienceChanged(PassageView.this,
                                    passage, getNewExperience(passage, formatExperience(newExperience), addOrModify));
                        }
                    }
                });
            }
            experienceEditDialog.show(fragmentManager, passage.getExperience());
        }

        private void onHistoryRecordViewerClick(Passage passage) {
            historyRecordDialog.show(fragmentManager, passage.getHistoryRecords());
        }
    };

    private OnTouchListener onPassageTouchListener = new OnTouchListener() {

        float x;
        float y;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    x = event.getX();
                    y = event.getY();
                } break;
                case MotionEvent.ACTION_UP: {
                    x = event.getX() - x;
                    y = Math.abs(event.getY() - y);
                    if (y < 5 && (x > -5 || x < 5)) {
                        enableFullscreen(rgContentPanel.getVisibility() == VISIBLE);
                        return true;
                    }
                    if (enableSlideSwitch) {
                        if (x > 0) {
                            if (x > y) {
                                showPrev();
                            }
                        } else {
                            if (-x > y) {
                                showNext();
                            }
                        }
                    }
                } break;
            }
            return false;
        }
    };

    private void enableFullscreen(boolean enable) {
        //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)svPassageCarrier.getLayoutParams();
        if (enable) {
            //params.removeRule(ABOVE);
            ibSetExperience.setVisibility(INVISIBLE);
            if (ibHistoryRecord != null) {
                ibHistoryRecord.setVisibility(INVISIBLE);
            }
            //rgContentPanel.setVisibility(INVISIBLE);
            rgContentPanel.setVisibility(GONE);
        } else {
            //params.addRule(ABOVE, R.id.rdo_grp_content_panel);
            ibSetExperience.setVisibility(VISIBLE);
            if (ibHistoryRecord != null) {
                ibHistoryRecord.setVisibility(VISIBLE);
            }
            rgContentPanel.setVisibility(VISIBLE);
        }
        //svPassageCarrier.setLayoutParams(params);
        if (onEnableFullscreenListener != null) {
            onEnableFullscreenListener.onEnableFullScreen(enable);
        }
    }

    public void setOnPassageShowListener(OnPassageShowListener l) {
        this.onPassageShowListener = l;
    }

    public interface OnPassageShowListener {
        void onShowPrev(Passage passage);
        void onShowNext(Passage passage);
    }

    public interface OnExperienceChangedListener {
        void onExperienceChanged(PassageView passageView, Passage passage, String newExperience);
    }

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            changeContent(adapter.currPassage(), checkedId);
        }
    };

    private void changeContent(Passage passage, @IdRes int checkedPanelId) {
        if (passage == null) {
            tvPassageContent.setText(getContext().getString(R.string.ppt_passage_content_null));
            tvPassageContent.setGravity(Gravity.CENTER);
        } else {
            switch (checkedPanelId) {
                case R.id.rdo_main_body:setContent(passage.getAuthorName(), passage.getAuthorDynasty(), passage.getContent(), R.string.ppt_content_null_main_body);break;
                case R.id.rdo_comments:setContent(null, null, passage.getComments(), R.string.ppt_content_null_comments);break;
                case R.id.rdo_translation:setContent(null, null, passage.getTranslation(), R.string.ppt_content_null_translation);break;
                case R.id.rdo_appreciation:setContent(null, null, passage.getAppreciation(), R.string.ppt_content_null_appreciation);break;
                case R.id.rdo_author:setContent(null, null, passage.getAuthorIntroduction(), R.string.ppt_content_null_author);break;
                case R.id.rdo_experience:setContent(null, null, passage.getExperience(), R.string.ppt_content_null_experience);break;
                default:setContent(null, null, null, R.string.ppt_content_panel_choose_error);break;
            }
        }
    }

    private void setContent(String authorName, String authorDynasty, String content, @StringRes int contentNullWarnInfo) {
        contentBuilder.setLength(0);
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
        if (TextUtils.isEmpty(content)) {
            content = getContext().getString(contentNullWarnInfo);
        }
        String realContent;
        if (contentBuilder.length() > 0) {
            contentBuilder.append(content);
            realContent = contentBuilder.toString();
        } else {
            realContent = content;
        }
        tvPassageContent.setText(realContent);
        tvPassageContent.setGravity(realContent.charAt(0) == '　' ? Gravity.START : Gravity.CENTER_HORIZONTAL);
    }

    public PassageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.group_passage_view, this);
        rgContentPanel = (RadioGroup)view.findViewById(R.id.rdo_grp_content_panel);
        rgContentPanel.setOnCheckedChangeListener(onCheckedChangeListener);
        svPassageCarrier = (ScrollView)view.findViewById(R.id.sv_passage_carrier);
        svPassageCarrier.setOnTouchListener(onPassageTouchListener);
        tvPassageTitle = (TextView)view.findViewById(R.id.tv_passage_header);
        tvPassageContent = (TextView)view.findViewById(R.id.tv_passage_content);
        adapter = emptyAdapter;
        setExperienceEditor();
    }

    private void setExperienceEditor() {
        ibSetExperience = (ImageButton)findViewById(R.id.ib_set_experience);
        ibSetExperience.setOnClickListener(onFunctionPanelClickListener);
    }

    private String getNewExperience(Passage passage, String newExperience, boolean addOrModify) {
        return !addOrModify || TextUtils.isEmpty(passage.getExperience()) ?
                newExperience :
                passage.getExperience() + '\n' + newExperience;
    }

    private String formatExperience(String experience) {
        StringBuilder builder = new StringBuilder(experience);
        Converter.guaranteeSBC(builder);
        for (int start = 0, size = builder.length(), spaceIndex; start < size;++start) {
            spaceIndex = start;
            //查询每段开头有几个空格
            while (spaceIndex < size && builder.charAt(spaceIndex) == '　') {
                ++spaceIndex;
            }
            //不足两个空格的补足
            while (spaceIndex - start < 2) {
                builder.insert(spaceIndex, '　');
                ++spaceIndex;
                ++size;
            }
            //多于两个空格的删除
            while (spaceIndex - start > 2) {
                builder.deleteCharAt(--spaceIndex);
                --size;
            }
            start = builder.indexOf("\n", spaceIndex);
            if (start == -1)
                break;
        }
        return builder.toString();
    }

    public void setFunctionBox(FragmentManager fragmentManager,
                               OnExperienceChangedListener l,
                               boolean startHistoryRecordViewer) {
        this.fragmentManager = fragmentManager;
        onExperienceChangedListener = l;
        if (startHistoryRecordViewer && ibHistoryRecord == null) {
            ViewStub vsHistoryRecord = (ViewStub) findViewById(R.id.vs_history_record);
            ibHistoryRecord = (ImageButton)vsHistoryRecord.inflate();
            ibHistoryRecord.setOnClickListener(onFunctionPanelClickListener);
            historyRecordDialog = new HistoryRecordDialog();
        }
    }

    private void showPassage(Passage passage, @IdRes int content) {
        setTitle(passage);
        showContent(passage, content);
    }

    private void showContent(Passage passage, @IdRes int content) {
        if (rgContentPanel.getCheckedRadioButtonId() == content) {
            changeContent(passage, content);
        } else {
            rgContentPanel.check(content);
        }
    }

    public void showContent(@IdRes int content) {
        rgContentPanel.check(content);
    }

    public void showCurr() {
        Passage passage = adapter.currPassage();
        if (passage == null) {
            Prompter.show(R.string.ppt_current_passage_empty);
            return;
        }
        showPassage(passage, R.id.rdo_main_body);
    }

    public void showNext() {
        Passage passage = adapter.nextPassage();
        if (passage == null) {
            Prompter.show(R.string.ppt_reach_last_passage_default);
            return;
        }
        showPassage(passage, R.id.rdo_main_body);
        if (onPassageShowListener != null) {
            onPassageShowListener.onShowNext(passage);
        }
    }

    public void showPrev() {
        Passage passage = adapter.prevPassage();
        if (passage == null) {
            Prompter.show(R.string.ppt_reach_first_passage_default);
            return;
        }
        showPassage(passage, R.id.rdo_main_body);
        if (onPassageShowListener != null) {
            onPassageShowListener.onShowPrev(passage);
        }
    }

    private void setTitle(Passage passage) {
        String title = null;
        if (passage != null) {
            title = passage.getOriginName();
            if (TextUtils.isEmpty(title)) {
                title = "无题";
            }
        }
        tvPassageTitle.setText(title);
    }

    public void setEnableSlideSwitch(boolean enable) {
        enableSlideSwitch = enable;
    }

    public void setOnEnableFullscreenListener(OnEnableFullscreenListener l) {
        onEnableFullscreenListener = l;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    public void empty() {
        tvPassageTitle.setText("");
        tvPassageContent.setText("");
    }

    public static abstract class Adapter {

        public abstract Passage currPassage();

        public abstract Passage nextPassage();

        public abstract Passage prevPassage();

        public int getPassageCount() {
            return 0;
        }
    }

    private static Adapter emptyAdapter = new Adapter() {
        @Override
        public Passage currPassage() {
            return null;
        }

        @Override
        public Passage nextPassage() {
            return null;
        }

        @Override
        public Passage prevPassage() {
            return null;
        }
    };
}
