package com.kikatech.go.ui;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * @author SkeeterWang Created on 2017/11/2.
 */
public class KikaMultiDexApplication extends MultiDexApplication {
    private static final String TAG = "KikaMultiDexApplication";

    private static Application sContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this); // install multidex
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Application getAppContext() {
        return sContext;
    }
}
