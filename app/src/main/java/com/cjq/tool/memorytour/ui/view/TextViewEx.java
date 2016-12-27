package com.cjq.tool.memorytour.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;

/**
 * Created by KAT on 2016/12/27.
 */
public class TextViewEx extends TextView {

    public TextViewEx(Context context) {
        this(context, null);
    }

    public TextViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewEx);
        int drawableWidth = typedArray.getDimensionPixelSize(R.styleable.TextViewEx_drawable_width, -1);
        int drawableHeight = typedArray.getDimensionPixelSize(R.styleable.TextViewEx_drawable_height, -1);
        setDrawableDimension(drawableWidth, drawableHeight);
        typedArray.recycle();
    }

    private void setDrawableDimension(int width, int height) {
        if (width == -1 || height == -1)
            return;
        Drawable[] drawables = getCompoundDrawables();
        for (Drawable drawable :
                drawables) {
            if (drawable != null) {
                drawable.setBounds(0, 0, width, height);
            }
        }
        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }
}
