package com.cjq.tool.memorytour.ui.toast;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cjq.tool.memorytour.R;


/**
 * Created by KAT on 2016/6/13.
 */
public class Prompter {

    protected Prompter() {
    }

    public static void init(Context context) {
        if (toast == null) {
            Prompter.context = context;
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.toast_prompt, null);
            label = (TextView)layout.findViewById(R.id.tv_toast);
            toast = new Toast(context);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
        }
    }

    public static void show(String information) {
        if (toast != null) {
            setInformation(information);
            toast.show();
        }
    }

    public static void show(int stringId) {
        show(context.getString(stringId));
    }

    private static void setInformation(String information) {
        label.setText(information);
    }

    private static Toast toast;
    private static TextView label;
    private static Context context;
}
