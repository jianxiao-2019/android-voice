package com.kikatech.voicesdktester.activities;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 01/02/2018.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);

//        if (PreferenceUtil.getBoolean(this, KEY_ENABLE_DEBUG_APP, false)) {
            Logger.d("MyApplication enable");
            PackageManager p = getPackageManager();
            p.setComponentEnabledSetting(new ComponentName(this, MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//        } else {
//            Logger.d("MyApplication disable");
//            PackageManager p = getPackageManager();
//            p.setComponentEnabledSetting(new ComponentName(this, MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//        }
    }
}
