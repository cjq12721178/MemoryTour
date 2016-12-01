package com.cjq.tool.memorytour.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;


/**
 * Created by KAT on 2016/6/12.
 */
public class LoadingDialog extends DialogFragment {

    private static final String TAG = "loading";
    private String description;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.dialog_loading, null);
        ImageView imageView = (ImageView)view.findViewById(R.id.iv_loading);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_loading);
        imageView.startAnimation(animation);
        TextView textView = (TextView)view.findViewById(R.id.tv_loading);
        textView.setText(description);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public int show(FragmentTransaction transaction, String description) {
        this.description = description;
        return super.show(transaction, TAG);
    }

    @Override
    public void show(FragmentManager manager, String description) {
        this.description = description;
        super.show(manager, TAG);
    }

    public void show(FragmentManager manager) {
        show(manager, null);
    }

    public int show(FragmentTransaction transaction) {
        return show(transaction, null);
    }
}
