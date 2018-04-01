package com.cjq.tool.memorytour;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.io.File;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testGetProjectName() {
        File path = getContext().getDatabasePath("MemoryStorage.db");
        String name = getContext().getApplicationInfo().loadLabel(getContext().getPackageManager()).toString();
        //Log.d("android test", "project name : " + name);
    }
}