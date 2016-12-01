package com.cjq.tool.memorytour.ui.activity;

import android.graphics.drawable.Drawable;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.fragment.BaseFragment;
import com.cjq.tool.memorytour.ui.fragment.CoverFragment;
import com.cjq.tool.memorytour.ui.fragment.EverydayReciteFragment;
import com.cjq.tool.memorytour.ui.fragment.HistoryRecordFragment;
import com.cjq.tool.memorytour.ui.fragment.NewMissionFragment;
import com.cjq.tool.memorytour.ui.fragment.SpecialFunctionFragment;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.Logger;
import com.cjq.tool.memorytour.util.Tag;

public class MainActivity extends AppCompatActivity implements BaseFragment.OnNotifyMainActivityListener {

    private static final String TAG_SELECTED_TEXT_VIEW_ID = "selected tv id";
    private int selectedTextColor;
    private int normalTextColor;
    private TextView lastSelectedView;
    private FrameLayout flFragmentStub;
    private LinearLayout liFunctionPanel;
    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPrompter();
        registerLogger();
        openDatabase();

        if (savedInstanceState == null) {
            launchCoverPage();
        }
        initFunctionPanel();
    }

    @Override
    protected void onDestroy() {
        closeDatabase();
        super.onDestroy();
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

    private void closeDatabase() {
        SQLiteManager.shutdown();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveSelectedPageState(outState);
    }

    private void saveSelectedPageState(Bundle outState) {
        outState.putInt(TAG_SELECTED_TEXT_VIEW_ID, lastSelectedView != null ? lastSelectedView.getId() : 0);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreSelectedPageState(savedInstanceState);
    }

    private void restoreSelectedPageState(Bundle savedInstanceState) {
        int selectedTvId = savedInstanceState.getInt(TAG_SELECTED_TEXT_VIEW_ID, 0);
        View tmp = selectedTvId != 0 ? findViewById(selectedTvId) : null;
        lastSelectedView = tmp instanceof TextView ? (TextView)tmp : null;
        setSelectedPage();
    }

    private void launchCoverPage() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_fragment_stub, createPage(Tag.FM_COVER))
                .commit();
    }

    private void initFunctionPanel() {
        flFragmentStub = (FrameLayout)findViewById(R.id.fl_fragment_stub);
        liFunctionPanel = (LinearLayout)findViewById(R.id.li_function_panel);
        selectedTextColor = getResources().getColor(R.color.text_function_panel_selected);
        normalTextColor = getResources().getColor(R.color.text_function_panel_normal);
        OnFunctionPanelClickListener onFunctionPanelClickListener = new OnFunctionPanelClickListener();
        setFunctionPanelTopIcon(R.id.tv_everyday_recite,
                R.drawable.ic_bg_everyday_recite_selected,
                R.drawable.ic_bg_everyday_recite_normal,
                Tag.FM_EVERYDAY_RECITE,
                onFunctionPanelClickListener);
        setFunctionPanelTopIcon(R.id.tv_new_mission,
                R.drawable.ic_bg_new_mission_selected,
                R.drawable.ic_bg_new_mission_normal,
                Tag.FM_NEW_MISSION,
                onFunctionPanelClickListener);
        setFunctionPanelTopIcon(R.id.tv_history_record,
                R.drawable.ic_bg_history_record_selected,
                R.drawable.ic_bg_history_record_normal,
                Tag.FM_HISTORY_RECORD,
                onFunctionPanelClickListener);
        setFunctionPanelTopIcon(R.id.tv_special_function,
                R.drawable.ic_bg_special_function_selected,
                R.drawable.ic_bg_special_function_normal,
                Tag.FM_SPECIAL_FUNCTION,
                onFunctionPanelClickListener);
    }

    private void setFunctionPanelTopIcon(int textViewId,
                                         int selectedDrawableId,
                                         int normalDrawableId,
                                         String pageTag,
                                         OnFunctionPanelClickListener l) {
        TextView tvFunction = (TextView)findViewById(textViewId);
        FunctionPanelViewHolder holder = new FunctionPanelViewHolder();
        holder.selectedTopIcon = getResources().getDrawable(selectedDrawableId);
        holder.normalTopIcon = getResources().getDrawable(normalDrawableId);
        holder.functionPage = getPage(pageTag);
        holder.pageTag = pageTag;
        tvFunction.setTag(holder);
        tvFunction.setOnClickListener(l);
    }

    public void setSelectedPage() {
        if (lastSelectedView == null || !(lastSelectedView.getTag() instanceof FunctionPanelViewHolder))
            return;
        BaseFragment selectedPage = ((FunctionPanelViewHolder)lastSelectedView.getTag()).functionPage;
        if (selectedPage == null)
            return;
        changePageLabelStyle(lastSelectedView, true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        setPageVisibility(fragmentManager, fragmentTransaction, selectedPage, Tag.FM_EVERYDAY_RECITE);
        setPageVisibility(fragmentManager, fragmentTransaction, selectedPage, Tag.FM_NEW_MISSION);
        setPageVisibility(fragmentManager, fragmentTransaction, selectedPage, Tag.FM_HISTORY_RECORD);
        setPageVisibility(fragmentManager, fragmentTransaction, selectedPage, Tag.FM_SPECIAL_FUNCTION);
        fragmentTransaction.commit();
    }

    private void setPageVisibility(FragmentManager fm, FragmentTransaction ft, BaseFragment selectedPage, String pageTag) {
        Fragment fragment = fm.findFragmentByTag(pageTag);
        if (!(fragment instanceof BaseFragment))
            return;
        BaseFragment page = (BaseFragment)fragment;
        if (page == selectedPage) {
            ft.show(page);
        } else {
            ft.hide(page);
        }
    }

    private BaseFragment getPage(String tag) {
        return  (BaseFragment)getSupportFragmentManager().findFragmentByTag(tag);
    }

    private BaseFragment createPage(String tag) {
        switch (tag) {
            case Tag.FM_COVER:return new CoverFragment();
            case Tag.FM_EVERYDAY_RECITE:return new EverydayReciteFragment();
            case Tag.FM_NEW_MISSION:return new NewMissionFragment();
            case Tag.FM_HISTORY_RECORD:return new HistoryRecordFragment();
            case Tag.FM_SPECIAL_FUNCTION:return new SpecialFunctionFragment();
            default:return null;
        }
    }

    private FunctionPanelViewHolder changePageLabelStyle(TextView v, boolean isSelected) {
        if (v == null)
            return null;
        if (!(v.getTag() instanceof FunctionPanelViewHolder))
            return null;
        FunctionPanelViewHolder holder = (FunctionPanelViewHolder)v.getTag();
        if (isSelected) {
            v.setTextColor(selectedTextColor);
            v.setCompoundDrawablesWithIntrinsicBounds(null, holder.selectedTopIcon, null, null);
        } else {
            v.setTextColor(normalTextColor);
            v.setCompoundDrawablesWithIntrinsicBounds(null, holder.normalTopIcon, null, null);
        }
        return holder;
    }

    private void switchPage(FunctionPanelViewHolder from, FunctionPanelViewHolder to) {
        if (to == null)
            return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (from != null) {
            transaction.hide(from.functionPage);
        } else {
            transaction.remove(getSupportFragmentManager().findFragmentById(R.id.fl_fragment_stub));
        }
        if (to.functionPage == null) {
            to.functionPage = createPage(to.pageTag);
            to.functionPage.setOnNotifyMainActivityListener(this);
        }
        if (to.functionPage.isAdded()) {
            transaction.show(to.functionPage);
        } else {
            transaction.add(R.id.fl_fragment_stub, to.functionPage, to.pageTag);
        }
        transaction.commit();
    }

    public void hideFunctionPanel(boolean enable) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)flFragmentStub.getLayoutParams();
        if (enable) {
            params.removeRule(RelativeLayout.ABOVE);
            liFunctionPanel.setVisibility(View.INVISIBLE);
        } else {
            params.addRule(RelativeLayout.ABOVE, R.id.li_function_panel);
            liFunctionPanel.setVisibility(View.VISIBLE);
        }
        flFragmentStub.setLayoutParams(params);
    }

    @Override
    public void onEnablePassageFullScreen(boolean enable) {
        hideFunctionPanel(enable);
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

    private class FunctionPanelViewHolder {
        public Drawable selectedTopIcon;
        public Drawable normalTopIcon;
        public BaseFragment functionPage;
        public String pageTag;
    }

    private class OnFunctionPanelClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (lastSelectedView == v || !(v instanceof TextView))
                return;

            TextView currentSelectedView = (TextView)v;
            FunctionPanelViewHolder lastHolder = changePageLabelStyle(lastSelectedView, false);
            FunctionPanelViewHolder currentHolder = changePageLabelStyle(currentSelectedView, true);
            switchPage(lastHolder, currentHolder);
            lastSelectedView = currentSelectedView;
        }
    }
}
