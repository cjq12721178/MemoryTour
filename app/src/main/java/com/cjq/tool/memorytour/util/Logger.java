package com.cjq.tool.memorytour.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.cjq.tool.memorytour.ui.toast.Prompter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by KAT on 2016/10/11.
 */
public class Logger {

    private static final String TAG = "memory tour";
    private static boolean isDebuggable;
    private static StringBuilder builder = new StringBuilder();

    protected Logger() {

    }

    public static void register(Context context) {
        isDebuggable = context != null ?
                (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0 :
                false;
    }

    public static void record(Exception e) {
        record(e.getMessage());
    }

    public static synchronized void record(String msg) {
        if (TextUtils.isEmpty(msg))
            return;
        if (isDebuggable) {
            printOnConsole(msg);
        } else {
            if (isSDCardWritable()) {
                writeIntoFile(decorateMessage(msg));
            } else {
                promptToUser(msg);
            }
        }
    }

    private static boolean isSDCardWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private static void printOnConsole(String info) {
        Log.d(TAG, info);
    }

    private static void writeIntoFile(String info) {
        try {
            File directory = new File(Environment.getExternalStorageDirectory() +
                    File.separator + TAG);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    Prompter.show("日志文件目录创建失败，无法记录异常信息");
                    return;
                }
            }
            File file = new File(directory.getAbsolutePath() +
                    File.separator + "ErrorInfo.txt");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Prompter.show("日志文件创建失败，无法记录异常信息");
                    return;
                }
            }
            FileWriter writer = new FileWriter(file, true);
            try {
                writer.write(info);
            } catch (IOException ioe) {
                Prompter.show("异常信息记录失败");
            } finally {
                writer.close();
            }
        } catch (IOException ioe) {
            Prompter.show("日志文件创建失败，无法记录异常信息");
        }
    }

    private static void promptToUser(String info) {
        Prompter.show(info);
    }

    @NonNull
    private static String decorateMessage(String msg) {
        //目前暂时简单的表示为“时间戳-错误信息”
        builder.setLength(0);
        builder.append(TimeFormatter.formatYearMonthDayHourMinuteSecond(System.currentTimeMillis()))
                .append(" —— ")
                .append(msg)
                .append('\n');
        return builder.toString();
    }
}
