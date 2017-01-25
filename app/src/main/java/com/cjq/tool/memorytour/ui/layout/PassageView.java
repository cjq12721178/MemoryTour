package com.cjq.tool.memorytour.ui.layout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
    private Adapter adapter;
    private OnPassageShowListener onPassageShowListener;
    private boolean enableSlideSwitch = false;
    private OnEnableFullscreenListener onEnableFullscreenListener;
    private FragmentManager fragmentManager;
    private ExperienceEditDialog experienceEditDialog;
    private OnExperienceChangedListener onExperienceChangedListener;
    private HistoryRecordDialog historyRecordDialog;
    private int prevPassageId;
    private int prevCheckedPanelId;
    private ContentBuilder[] contentBuilders;
    private Handler handler;
    private ScrollPositionKeeper scrollPositionKeeper = new ScrollPositionKeeper();

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

    private void enableFullscreen(boolean enable) {
        if (enable) {
            ibSetExperience.setVisibility(INVISIBLE);
            if (ibHistoryRecord != null) {
                ibHistoryRecord.setVisibility(INVISIBLE);
            }
            rgContentPanel.setVisibility(GONE);
        } else {
            ibSetExperience.setVisibility(VISIBLE);
            if (ibHistoryRecord != null) {
                ibHistoryRecord.setVisibility(VISIBLE);
            }
            rgContentPanel.setVisibility(VISIBLE);
        }
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
                handler.post(scrollPositionKeeper);
            }
            prevPassageId = currPassageId;
        }
    }

    public PassageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        handler = new Handler();
        initContentBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.group_passage_view, this);
        rgContentPanel = (RadioGroup)view.findViewById(R.id.rdo_grp_content_panel);
        rgContentPanel.setOnCheckedChangeListener(onCheckedChangeListener);
        svPassageCarrier = (ScrollView)view.findViewById(R.id.sv_passage_carrier);
        final TouchHandler touchHandler = new TouchHandler(handler);
        svPassageCarrier.setOnTouchListener(touchHandler);
        tvPassageTitle = (TextView)view.findViewById(R.id.tv_passage_header);
        tvPassageContent = (TextView)view.findViewById(R.id.tv_passage_content);
        tvPassageContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
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
                //tvPassageContent.setTextIsSelectable(false);
                //touchHandler.setInLongPress(false);
                //touchHandler.setTextIsSelectable(false);
                //handler.sendEmptyMessage()
                touchHandler.finishSelectMode();
            }
        });
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

    private int getCurrentBrowsePosition() {
        return svPassageCarrier.getScrollY();
    }

    private void setBrowsePosition(int position) {
        svPassageCarrier.setScrollY(position);
        //svPassageCarrier.scrollTo(0, position);
        //svPassageCarrier.smoothScrollTo(0, position);
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
        //rgContentPanel.check(content);
        showContent(adapter.currPassage(), content);
    }

    public void showCurr() {
        Passage passage = adapter.currPassage();
        if (passage == null) {
            Prompter.show(R.string.ppt_current_passage_empty);
            return;
        }
        //int pos = getCurrentBrowsePosition();
        showPassage(passage, R.id.rdo_main_body);
        //setBrowsePosition(pos);
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

    public void clear() {
        tvPassageTitle.setText("");
        tvPassageContent.setText("");
    }

    private void initContentBuilder(Context context) {
        contentBuilders = new ContentBuilder[7];
        contentBuilders[0] = new MainBodyBuilder(context.getString(R.string.ppt_content_null_main_body));
        contentBuilders[1] = new CommentsBuilder(context.getString(R.string.ppt_content_null_comments));
        contentBuilders[2] = new TranslationBuilder(context.getString(R.string.ppt_content_null_translation));
        contentBuilders[3] = new AppreciationBuilder(context.getString(R.string.ppt_content_null_appreciation));
        contentBuilders[4] = new AuthorIntroductionBuilder(context.getString(R.string.ppt_content_null_appreciation));
        contentBuilders[5] = new ExperienceBuilder(context.getString(R.string.ppt_content_null_experience));
        contentBuilders[6] = new NullBuilder(context.getString(R.string.ppt_content_panel_choose_error));
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
            //builder.setScrollPos(0);
            builder.reset();
        }
    }

    private class TouchHandler
            extends Handler
            implements OnTouchListener {

        float downX;
        float downY;
        boolean inLongPress;
        boolean textIsSelectable;
        boolean inLongPressRegion;
        int start = -1;
        String selectedText;
        static final int LONG_PRESS = 1;
        static final int SELECT_FINISH = 2;
        static final int SLIDE_SWITCH_THRESHOLD = 100;
        static final int CLICK_THRESHOLD = 5;
        final float maxTouchSlopSquare;
        final long longPressTimeout;
        //final BackgroundColorSpan colorSpan = new BackgroundColorSpan(Color.BLUE);
        //PopupWindow selectPrompter;

        TouchHandler(Handler handler) {
            super(handler.getLooper());
            ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
            int touchSlop = viewConfiguration.getScaledTouchSlop();
            maxTouchSlopSquare = touchSlop * touchSlop;
            longPressTimeout = viewConfiguration.getLongPressTimeout();

//            LinearLayout selectLayout = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.popup_select_prompter, null);
//            selectLayout.findViewById(R.id.tv_copy).setOnClickListener(this);
//            selectLayout.findViewById(R.id.tv_select_all).setOnClickListener(this);
//            selectLayout.findViewById(R.id.tv_cancel).setOnClickListener(this);
//            selectPrompter = new PopupWindow(selectLayout,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT);
//            TextView tvSelectPrompter = (TextView) View.inflate(getContext(), R.layout.popup_select_prompter, null);
//            tvSelectPrompter.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (!TextUtils.isEmpty(selectedText)) {
//                        inLongPress = false;
//                        copyPrompter.dismiss();
//                        tvPassageContent.getEditableText().removeSpan(colorSpan);
//                        ClipboardManager clipboardManager = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, selectedText));
//                    }
//                }
//            });
//            copyPrompter = new PopupWindow(tvSelectPrompter,
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            copyPrompter.setFocusable(true);
        }

        public void setTextIsSelectable(boolean textIsSelectable) {
            this.textIsSelectable = textIsSelectable;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LONG_PRESS) {
                inLongPress = true;
                textIsSelectable = true;
                //selectPrompter.showAtLocation(svPassageCarrier, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                //tvPassageContent.setTextIsSelectable(true);
                tvPassageContent.setTextIsSelectable(true);
                tvPassageContent.performLongClick();
                //Prompter.show("onLongClick");
            } else if (msg.what == SELECT_FINISH) {
                tvPassageContent.setTextIsSelectable(false);
                inLongPress = false;
            }
            super.handleMessage(msg);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            if (inLongPress)
//                return onTouchInLongPress(event);
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
//                    if (inLongPress) {
//                        inLongPress = false;
//                        break;
//                    }
                    if (inLongPressRegion) {
                        removeMessages(LONG_PRESS);
                    }
                    float deltaX = event.getX() - downX;
                    float absDeltaY = Math.abs(event.getY() - downY);
                    float absDeltaX = Math.abs(deltaX);
                    if (isInClickRegion(absDeltaX, absDeltaY)) {
                        char c = getClickChineseCharacter(downX, downY);
                        if (c == 0) {
                            enableFullscreen(rgContentPanel.getVisibility() == VISIBLE);
                        } else {
                            Prompter.show(String.valueOf(c));
                        }
                        return true;
                    }
                    if (enableSlideSwitch && absDeltaX > SLIDE_SWITCH_THRESHOLD) {
                        if (deltaX > absDeltaY) {
                            showPrev();
                            return true;
                        } else if (-deltaX > absDeltaY) {
                            showNext();
                            return true;
                        }
                    }
                } break;
            }
            return false;
        }

//        boolean onTouchInLongPress(MotionEvent event) {
//            int action = event.getActionMasked();
//            switch (action) {
//                case MotionEvent.ACTION_DOWN: {
//                    start = getSelectOffset(event.getX(), event.getY());
//                } break;
//                case MotionEvent.ACTION_MOVE:
//                case MotionEvent.ACTION_UP: {
//                    if (start == -1)
//                        break;
//                    float x = event.getX();
//                    float y = event.getY();
//                    int end = getSelectOffset(x, y);
//                    if (start == end) {
//                        if (action == MotionEvent.ACTION_UP) {
//                            start = -1;
//                        }
//                    } else {
//                        if (start > end) {
//                            int tmp = start;
//                            start = end;
//                            end = tmp;
//                        }
//                        Editable editable = tvPassageContent.getEditableText();
//                        editable.setSpan(colorSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                        if (action == MotionEvent.ACTION_UP) {
//                            selectedText = editable.subSequence(start, end).toString();
//                            start = -1;
//                            //copyPrompter.showAtLocation(svPassageCarrier, Gravity.NO_GRAVITY, (int)x, (int)y);
//                        }
//                    }
//                } break;
//            }
//            return true;
//        }

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

        int getSelectOffset(float x, float y) {
            Layout layout = tvPassageContent.getLayout();
            int line = layout.getLineForVertical(getRealVertical(y));
            return layout.getOffsetForHorizontal(line, x);
        }

        int getRealVertical(float y) {
            return svPassageCarrier.getScrollY() + (int)y - tvPassageTitle.getBottom();
        }

        public void setInLongPress(boolean inLongPress) {
            this.inLongPress = inLongPress;
        }

        public void finishSelectMode() {
            sendEmptyMessage(SELECT_FINISH);
        }

//        @Override
//        public void onClick(View v) {
//            int id = v.getId();
//            if (id == R.id.tv_copy || id == R.id.tv_cancel) {
//                if (id == R.id.tv_copy && !TextUtils.isEmpty(selectedText)) {
//                    ClipboardManager clipboardManager = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, selectedText));
//                }
//                inLongPress = false;
//                selectPrompter.dismiss();
//                tvPassageContent.getEditableText().removeSpan(colorSpan);
//            } else if (id == R.id.tv_select_all) {
//                Editable editable = tvPassageContent.getEditableText();
//                if (editable.length() > 0) {
//                    editable.setSpan(colorSpan, 0, editable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                }
//            }
//        }
    };

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
            //String realContent;
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
