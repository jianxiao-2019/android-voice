package com.kikatech.go.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.kikatech.go.services.OobeService;
import com.kikatech.go.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

/**
 * @author SkeeterWang Created on 2017/11/2.
 */
public class KikaMultiDexApplication extends MultiDexApplication {
    private static final String TAG = "KikaMultiDexApplication";


    private static Application sContext;

    private static Activity sActivity;
    private static Timer mActivityTransitionTimer;
    private static TimerTask mActivityTransitionTimerTask;
    private static boolean wasInBackground = true;
    private static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 1000;


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


    // ---------- Application Background/Foreground Handling ----------

    public static void onActivityResume(Activity activity) {
        sActivity = activity;

        if (wasInBackground) {
            onAppWentForeground();
        }

        stopActivityTransitionTimer();
    }

    public static void onActivityPause(Activity activity) {
        sActivity = activity;
        startActivityTransitionTimer();
    }

    private static void startActivityTransitionTimer() {
        if (mActivityTransitionTimerTask != null) {
            mActivityTransitionTimerTask.cancel();
        }
        if (mActivityTransitionTimer != null) {
            mActivityTransitionTimer.cancel();
        }

        mActivityTransitionTimer = new Timer();
        mActivityTransitionTimerTask = new TimerTask() {
            @Override
            public void run() {
                onAppWentBackground();
                wasInBackground = true;
            }
        };
        mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    private static void stopActivityTransitionTimer() {
        if (mActivityTransitionTimerTask != null) {
            mActivityTransitionTimerTask.cancel();
        }
        if (mActivityTransitionTimer != null) {
            mActivityTransitionTimer.cancel();
        }
        wasInBackground = false;
    }

    private static void onAppWentBackground() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Application went background");
        }
    }

    private static void onAppWentForeground() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Application went foreground");
        }
    }

    public static Activity getCurrentActivity() {
        return sActivity;
    }

    public static boolean isApplicationInForeground() {
        return !wasInBackground;
    }
}
