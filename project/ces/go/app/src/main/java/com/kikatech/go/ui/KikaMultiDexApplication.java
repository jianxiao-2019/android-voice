package com.kikatech.go.ui;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.kikatech.go.util.LogUtil;

import io.fabric.sdk.android.Fabric;

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
        Fabric.with(new Fabric.Builder(this)
                .kits(new Crashlytics(), new CrashlyticsNdk())
                .debuggable(LogUtil.DEBUG)
                .build());
        sContext = this;

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static Application getAppContext() {
        return sContext;
    }
}
