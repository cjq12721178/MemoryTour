package com.cjq.tool.memorytour.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by KAT on 2016/11/10.
 */
public class Converter {

    private Converter() {
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, int pxValue) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                pxValue, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    //转全角的函数(SBC case)
    //全角空格为12288，半角空格为32
    //其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
    public static void guaranteeSBC(StringBuilder dbc) {
        if (dbc != null) {
            for (int i = 0, end = dbc.length();i < end;++i) {
                char ch = dbc.charAt(i);
                if (ch == 32) {
                    dbc.setCharAt(i, (char)12288);
                } else if (ch > 32 && ch < 127) {
                    dbc.setCharAt(i, (char)(ch + 65248));
                }
            }
        }
    }

    public static String toSBC(StringBuilder dbc) {
        guaranteeSBC(dbc);
        return dbc != null ? dbc.toString() : null;
    }
    
    public static String toSBC(String dbc) {
        return dbc != null ? toSBC(new StringBuilder(dbc)) : null;
    }
}