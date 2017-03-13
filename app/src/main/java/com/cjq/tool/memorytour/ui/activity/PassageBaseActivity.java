package com.cjq.tool.memorytour.ui.activity;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.bean.UserInfo;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.dialog.ExperienceEditDialog;
import com.cjq.tool.memorytour.ui.dialog.HistoryRecordDialog;
import com.cjq.tool.memorytour.ui.dialog.ReciteTestDialog;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.Converter;
import com.cjq.tool.memorytour.util.Logger;

public abstract class PassageBaseActivity extends AppCompatActivity {

    protected static final int SELECT_PASSAGE_COUNT = 3;
    private TextView tvPassageTitle;
    private TextView tvPassageContent;
    private TextView tvPromptInformation;
    private RelativeLayout rlPassageAuxiliary;
    private RadioGroup rgContentPanel;
    private ScrollView svPassageCarrier;
    private ImageButton ibSetExperience;
    private ImageButton ibHistoryRecord;
    private ImageButton ibAuxiliaryFunction;
    private TouchHandler touchHandler;
    private ExperienceEditDialog experienceEditDialog;
    private HistoryRecordDialog historyRecordDialog;
    private boolean enableSlideSwitch = false;
    private boolean isFullScreen = false;
    private int currentPassageIndex = -1;
    private int prevPassageId;
    private int prevCheckedPanelId;
    private static ContentBuilder[] contentBuilders;
    private ScrollPositionKeeper scrollPositionKeeper = new ScrollPositionKeeper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passage_base);

        touchHandler = new TouchHandler();
        initContentBuilder();
        rlPassageAuxiliary = (RelativeLayout)findViewById(R.id.rl_passage_auxiliary);
        rgContentPanel = (RadioGroup)findViewById(R.id.rdo_grp_content_panel);
        rgContentPanel.setOnCheckedChangeListener(touchHandler);
        tvPromptInformation = (TextView)findViewById(R.id.tv_memory_situation);
        svPassageCarrier = (ScrollView)findViewById(R.id.sv_passage_carrier);
        svPassageCarrier.setOnTouchListener(touchHandler);
        tvPassageTitle = (TextView)findViewById(R.id.tv_passage_header);
        tvPassageContent = (TextView)findViewById(R.id.tv_passage_content);
        tvPassageContent.setCustomSelectionActionModeCallback(touchHandler);
        ibSetExperience = (ImageButton)findViewById(R.id.ib_set_experience);
        ibSetExperience.setOnClickListener(touchHandler);
        ibHistoryRecord = (ImageButton)findViewById(R.id.ib_history_record);
        ibHistoryRecord.setOnClickListener(touchHandler);
        ibAuxiliaryFunction = (ImageButton)findViewById(R.id.ib_auxiliary_function);
        ibAuxiliaryFunction.setOnClickListener(touchHandler);
    }

    protected void setPromptInformation(String info) {
        if (!TextUtils.equals(tvPromptInformation.getText(), info)) {
            tvPromptInformation.setText(info);
            tvPromptInformation.setVisibility(TextUtils.isEmpty(info) ? View.GONE : View.VISIBLE);
        }
    }

    protected void setAuxiliaryFunctionImageResource(@DrawableRes int backgroundId) {
        ibAuxiliaryFunction.setImageResource(backgroundId);
    }

    protected void setAuxiliaryFunctionEnable(boolean enabled) {
        ibAuxiliaryFunction.setEnabled(enabled);
    }

    protected void setAuxiliaryFunctionVisibility(int visibility) {
        ibAuxiliaryFunction.setVisibility(visibility);
    }

    private void changeContent(Passage passage, @IdRes int checkedPanelId) {
        if (passage == null) {
            tvPassageContent.setText(getString(R.string.ppt_passage_content_null));
            tvPassageContent.setGravity(Gravity.CENTER);
        } else {
            //记录滚动位置
            int currPassageId = passage.getId();
            if (prevPassageId != currPassageId) {
                clearContentBuilderRecord();
            } else {
                getContentBuilder(prevCheckedPanelId).setScrollPos(getCurrentBrowsePosition());
            }
            prevCheckedPanelId = checkedPanelId;
            //设置实际内容
            ContentBuilder contentBuilder = getContentBuilder(checkedPanelId);
            String realContent = contentBuilder.build(passage);
            tvPassageContent.setText(realContent);
            tvPassageContent.setGravity(realContent.charAt(0) == '　' ? Gravity.START : Gravity.CENTER_HORIZONTAL);
            //设置滚动位置
            if (prevPassageId == currPassageId) {
                scrollPositionKeeper.setPosition(contentBuilder.getScrollPos());
                touchHandler.post(scrollPositionKeeper);
            }
            prevPassageId = currPassageId;
        }
    }

    private String getNewExperience(Passage passage, String newExperience, boolean addOrModify) {
        return !addOrModify || TextUtils.isEmpty(passage.getExperience()) ?
                newExperience :
                passage.getExperience() + '\n' + newExperience;
    }

    @NonNull
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

    private int getCurrentBrowsePosition() {
        return svPassageCarrier.getScrollY();
    }

    private void setBrowsePosition(int position) {
        svPassageCarrier.setScrollY(position);
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

    //注意，该方法只能在当前章节的各个部分进行切换
    public void showContent(@IdRes int content) {
        showContent(getCurrentPassage(), content);
    }

    public void showCurrentPassage() {
        Passage passage = getCurrentPassage();
        if (passage == null) {
            Prompter.show(R.string.ppt_current_passage_empty);
            return;
        }
        showPassage(passage, R.id.rdo_main_body);
        onShowCurrentPassage(passage);
    }

    public void showNextPassage() {
        Passage passage = nextPassage();
        if (passage == null) {
            Prompter.show(R.string.ppt_reach_last_passage_default);
            return;
        }
        //++currentPassageIndex;
        //setPassageCurrentIndex(currentPassageIndex + 1);
        showPassage(passage, R.id.rdo_main_body);
        onShowNextPassage(passage);
    }

    public void showPreviousPassage() {
        Passage passage = previousPassage();
        if (passage == null) {
            Prompter.show(R.string.ppt_reach_first_passage_default);
            return;
        }
        //--currentPassageIndex;
        //setPassageCurrentIndex(currentPassageIndex - 1);
        showPassage(passage, R.id.rdo_main_body);
        onShowPreviousPassage(passage);
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

    private void initContentBuilder() {
        if (contentBuilders != null) {
            clearContentBuilderRecord();
        } else {
            contentBuilders = new ContentBuilder[7];
            contentBuilders[0] = new MainBodyBuilder(getString(R.string.ppt_content_null_main_body));
            contentBuilders[1] = new CommentsBuilder(getString(R.string.ppt_content_null_comments));
            contentBuilders[2] = new TranslationBuilder(getString(R.string.ppt_content_null_translation));
            contentBuilders[3] = new AppreciationBuilder(getString(R.string.ppt_content_null_appreciation));
            contentBuilders[4] = new AuthorIntroductionBuilder(getString(R.string.ppt_content_null_appreciation));
            contentBuilders[5] = new ExperienceBuilder(getString(R.string.ppt_content_null_experience));
            contentBuilders[6] = new NullBuilder(getString(R.string.ppt_content_panel_choose_error));
        }
    }

    private ContentBuilder getContentBuilder(@IdRes int checkedPanelId) {
        switch (checkedPanelId) {
            case R.id.rdo_main_body:return contentBuilders[0];
            case R.id.rdo_comments:return contentBuilders[1];
            case R.id.rdo_translation:return contentBuilders[2];
            case R.id.rdo_appreciation:return contentBuilders[3];
            case R.id.rdo_author:return contentBuilders[4];
            case R.id.rdo_experience:return contentBuilders[5];
            default:return contentBuilders[6];
        }
    }

    private void clearContentBuilderRecord() {
        for (ContentBuilder builder :
                contentBuilders) {
            builder.reset();
        }
    }

    public void clearPassageView() {
        tvPassageTitle.setText("");
        tvPassageContent.setText("");
    }

    public Passage getPreviousPassage() {
        return currentPassageIndex <= 0 ?
                null :
                getPassage(currentPassageIndex - 1);
    }

    public Passage getCurrentPassage() {
        return currentPassageIndex == -1 ?
                null :
                getPassage(currentPassageIndex);
    }

    public Passage getNextPassage() {
        return currentPassageIndex + 1 >= getPassageCount() ?
                null :
                getPassage(currentPassageIndex + 1);
    }

    public Passage previousPassage() {
        Passage passage = getPreviousPassage();
        if (passage != null) {
            --currentPassageIndex;
        }
        return passage;
    }

    public Passage nextPassage() {
        Passage passage = getNextPassage();
        if (passage != null) {
            ++currentPassageIndex;
        }
        return passage;
    }

    //返回设置后的currentPassageIndex，可以将其与设置值比较判断是否设置成功
    public int setPassageCurrentIndex(int index) {
        if (index >= -1 &&
                index < getPassageCount() &&
                currentPassageIndex != index) {
            currentPassageIndex = index;
        }
        return currentPassageIndex;
    }

    protected abstract Passage getPassage(int index);

    protected abstract int getPassageCount();

    protected void onShowNextPassage(Passage passage) {

    }

    protected void onShowCurrentPassage(Passage passage) {

    }

    protected void onShowPreviousPassage(Passage passage) {

    }

    public void performAuxiliaryFunctionClick() {
        onAuxiliaryFunctionClick(ibAuxiliaryFunction);
    }

    protected void onAuxiliaryFunctionClick(ImageButton ib) {

    }

    private class ModifyExperienceTask extends AsyncTask<Object, Void, Boolean> {

        private Passage passage;
        private String experience;

        @Override
        protected Boolean doInBackground(Object... params) {
            if (params == null || params.length != 2 ||
                    !(params[0] instanceof Passage) ||
                    !(params[1] instanceof String))
                return null;
            return SQLiteManager.updateExperience(passage = (Passage)params[0],
                    experience = (String)params[1]);
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (isSuccess) {
                passage.setExperience(experience);
                showContent(R.id.rdo_experience);
            } else {
                Prompter.show(R.string.ppt_experience_update_failed);
            }
        }
    }

    private class TouchHandler
            extends Handler
            implements View.OnTouchListener,
            View.OnClickListener,
            RadioGroup.OnCheckedChangeListener,
            ActionMode.Callback {

        float downX;
        float downY;
        boolean inLongPress;
        boolean inLongPressRegion;
        static final int LONG_PRESS = 1;
        static final int SELECT_FINISH = 2;
        static final int SLIDE_SWITCH_THRESHOLD = 100;
        static final int CLICK_THRESHOLD = 5;
        final float maxTouchSlopSquare;
        final long longPressTimeout;

        TouchHandler() {
            super();
            ViewConfiguration viewConfiguration = ViewConfiguration.get(PassageBaseActivity.this);
            int touchSlop = viewConfiguration.getScaledTouchSlop();
            maxTouchSlopSquare = touchSlop * touchSlop;
            longPressTimeout = viewConfiguration.getLongPressTimeout();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LONG_PRESS) {
                inLongPress = true;
                tvPassageContent.setTextIsSelectable(true);
                tvPassageContent.performLongClick();
            } else if (msg.what == SELECT_FINISH) {
                tvPassageContent.setTextIsSelectable(false);
                inLongPress = false;
            }
            super.handleMessage(msg);
        }

        //处理章节内容部位的触摸事件，包括：
        // 1. 单击查字
        // 2. 长按选择
        // 3. 垂直滑动显示章节内容
        // 4. 水平滑动切换章节（可选）
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (inLongPress)
                return false;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    downY = event.getY();
                    inLongPressRegion = true;
                    removeMessages(LONG_PRESS);
                    sendEmptyMessageDelayed(LONG_PRESS, longPressTimeout);
                } break;
                case MotionEvent.ACTION_MOVE: {
                    if (inLongPressRegion) {
                        if (!isInLongPressRegion(event.getY() - downY, event.getX() - downX)) {
                            inLongPressRegion = false;
                            removeMessages(LONG_PRESS);
                        }
                    }
                } break;
                case MotionEvent.ACTION_UP: {
                    if (inLongPressRegion) {
                        removeMessages(LONG_PRESS);
                    }
                    float deltaX = event.getX() - downX;
                    float absDeltaY = Math.abs(event.getY() - downY);
                    float absDeltaX = Math.abs(deltaX);
                    if (isInClickRegion(absDeltaX, absDeltaY)) {
                        char c = getClickChineseCharacter(downX, downY);
                        if (c == 0) {
                            enableFullscreen(!isFullScreen);
                        } else {
                            //Prompter.show(String.valueOf(c));
                            //TODO 单击查字
                        }
                        return true;
                    }
                    if (enableSlideSwitch && absDeltaX > SLIDE_SWITCH_THRESHOLD) {
                        if (deltaX > absDeltaY) {
                            showPreviousPassage();
                            return true;
                        } else if (-deltaX > absDeltaY) {
                            showNextPassage();
                            return true;
                        }
                    }
                } break;
            }
            return false;
        }

        boolean isInLongPressRegion(float deltaX, float deltaY) {
            return deltaX * deltaX + deltaY * deltaY < maxTouchSlopSquare;
        }

        boolean isInClickRegion(float absDeltaX, float absDeltaY) {
            return absDeltaY < CLICK_THRESHOLD && absDeltaX < CLICK_THRESHOLD;
        }

        char getClickChineseCharacter(float x, float y) {
            int vertical = getRealVertical(y);
            if (vertical <= 0)
                return 0;
            Layout layout = tvPassageContent.getLayout();
            int line = layout.getLineForVertical(vertical);
            int position = layout.getOffsetForHorizontal(line, x) - 1;
            CharSequence cs = tvPassageContent.getText();
            if (position < 0 || position >= cs.length())
                return 0;
            char result = cs.charAt(position);
            if (!isChineseCharacter(result))
                return 0;
            if (layout.getLineVisibleEnd(line) - 1 == position &&
                    layout.getPrimaryHorizontal(position + 1) * 2 - layout.getPrimaryHorizontal(position) < x)
                return 0;
            return result;
        }

        boolean isChineseCharacter(char c) {
            return (c >= '\u4e00' && c <= '\u9fa5') ||
                    (c >= '\uf900' && c <= '\ufa2d');
        }

        int getRealVertical(float y) {
            return svPassageCarrier.getScrollY() + (int)y - tvPassageTitle.getBottom();
        }

        void enableFullscreen(boolean enable) {
            isFullScreen = enable;
            if (enable) {
                rlPassageAuxiliary.setVisibility(View.GONE);
                rgContentPanel.setVisibility(View.GONE);
            } else {
                rlPassageAuxiliary.setVisibility(View.VISIBLE);
                rgContentPanel.setVisibility(View.VISIBLE);
            }
        }

        void finishSelectMode() {
            sendEmptyMessage(SELECT_FINISH);
        }

        //处理各类点击事件，目前以功能面板为主（包括查看历史记忆记录，添加或修改心得体会）
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.ib_set_experience ||
                    id == R.id.ib_history_record) {
                onFunctionPanelClick(id);
            } else if (id == R.id.ib_auxiliary_function) {
                onAuxiliaryFunctionClick(ibAuxiliaryFunction);
            }
        }

        void onFunctionPanelClick(@IdRes int id) {
            final Passage passage = getCurrentPassage();
            if (passage == null) {
                Prompter.show(R.string.ppt_passage_content_null);
            } else {
                switch (id) {
                    case R.id.ib_set_experience:onExperienceEditorClick(passage);break;
                    case R.id.ib_history_record:onHistoryRecordViewerClick(passage);break;
                    default:
                        Logger.record("未点中任何功能按钮");break;
                }
            }
        }

        void onExperienceEditorClick(final Passage passage) {
            if (experienceEditDialog == null) {
                experienceEditDialog = new ExperienceEditDialog();
                experienceEditDialog.setOnSetExperienceListener(new ExperienceEditDialog.OnSetExperienceListener() {
                    @Override
                    public void onSetExperience(String experience, boolean addOrModify) {
                        String newExperience = getNewExperience(passage, formatExperience(experience), addOrModify);
                        if (!newExperience.equals(passage.getExperience())) {
                            ModifyExperienceTask task = new ModifyExperienceTask();
                            task.execute(passage, newExperience);
                        }
                    }
                });
            }
            experienceEditDialog.show(getSupportFragmentManager(), passage.getExperience());
        }

        void onHistoryRecordViewerClick(Passage passage) {
            if (historyRecordDialog == null) {
                historyRecordDialog = new HistoryRecordDialog();
            }
            historyRecordDialog.show(getSupportFragmentManager(), passage.getHistoryRecords());
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            changeContent(getCurrentPassage(), checkedId);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            finishSelectMode();
        }
    }

    private class ScrollPositionKeeper implements Runnable {

        private int position;

        public ScrollPositionKeeper setPosition(int position) {
            this.position = position;
            return this;
        }

        @Override
        public void run() {
            setBrowsePosition(position);
        }
    }

    private static abstract class ContentBuilder {

        private static StringBuilder builder = new StringBuilder(256);
        private int scrollPos;
        protected String value;
        private final String nullContentWarnInfo;

        public ContentBuilder(String nullContentWarnInfo) {
            this.nullContentWarnInfo = nullContentWarnInfo;
        }

        public int getScrollPos() {
            return scrollPos;
        }

        public void setScrollPos(int position) {
            scrollPos = position;
        }

        public void reset() {
            setScrollPos(0);
            value = null;
        }

        public abstract String build(Passage passage);

        protected String build(String authorName, String authorDynasty, String content) {
            if (value != null)
                return value;
            builder.setLength(0);
            if (!TextUtils.isEmpty(authorDynasty)) {
                builder.append('[')
                        .append(authorDynasty)
                        .append(']')
                        .append(' ');
            }
            if (!TextUtils.isEmpty(authorName)) {
                builder.append(authorName)
                        .append('\n')
                        .append('\n');
            }
            if (TextUtils.isEmpty(content)) {
                content = nullContentWarnInfo;
            }
            if (builder.length() > 0) {
                builder.append(content);
                value = builder.toString();
            } else {
                value = content;
            }
            return value;
        }
    }

    private static class MainBodyBuilder extends ContentBuilder {

        public MainBodyBuilder(String nullContentWarnInfo) {
            super(nullContentWarnInfo);
        }

        @Override
        public String build(Passage passage) {
            return build(passage.getAuthorName(), passage.getAuthorDynasty(), passage.getContent());
        }
    }

    private static class CommentsBuilder extends ContentBuilder {

        public CommentsBuilder(String nullContentWarnInfo) {
            super(nullContentWarnInfo);
        }

        @Override
        public String build(Passage passage) {
            return build(null, null, passage.getComments());
        }
    }

    private static class TranslationBuilder extends ContentBuilder {

        public TranslationBuilder(String nullContentWarnInfo) {
            super(nullContentWarnInfo);
        }

        @Override
        public String build(Passage passage) {
            return build(null, null, passage.getTranslation());
        }
    }

    private static class AppreciationBuilder extends ContentBuilder {

        public AppreciationBuilder(String nullContentWarnInfo) {
            super(nullContentWarnInfo);
        }

        @Override
        public String build(Passage passage) {
            return build(null, null, passage.getAppreciation());
        }
    }

    private static class AuthorIntroductionBuilder extends ContentBuilder {

        public AuthorIntroductionBuilder(String nullContentWarnInfo) {
            super(nullContentWarnInfo);
        }

        @Override
        public String build(Passage passage) {
            return build(null, null, passage.getAuthorIntroduction());
        }
    }

    private static class ExperienceBuilder extends ContentBuilder {

        public ExperienceBuilder(String nullContentWarnInfo) {
            super(nullContentWarnInfo);
        }

        @Override
        public String build(Passage passage) {
            value = null;
            return build(null, null, passage.getExperience());
        }
    }

    private static class NullBuilder extends ContentBuilder {

        public NullBuilder(String nullContentWarnInfo) {
            super(nullContentWarnInfo);
        }

        @Override
        public String build(Passage passage) {
            return build(null, null, null);
        }
    }
}
