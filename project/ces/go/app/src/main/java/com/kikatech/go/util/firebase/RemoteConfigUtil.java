package com.kikatech.go.util.firebase;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.preference.GlobalPref;
import com.kikatech.go.util.timer.CountingTimer;

import java.util.HashMap;

/**
 * @author SkeeterWang Created on 2018/4/2.
 * @see <a href="https://firebase.google.com/docs/reference/android/com/google/firebase/remoteconfig/FirebaseRemoteConfig">Firebase Document</a>
 */

public class RemoteConfigUtil {
    private static final String TAG = "RemoteConfigUtil";

    /**
     * cacheExpiration is set to 0 so each fetch will retrieve values from the server.
     **/
    private static final long CACHE_EXPIRATION = 600; // seconds

    private static final long TIME_OUT = 5000;


    private static RemoteConfigUtil sIns;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public static synchronized RemoteConfigUtil getIns() {
        if (sIns == null) {
            sIns = new RemoteConfigUtil();
        }
        return sIns;
    }

    private RemoteConfigUtil() {
        // Get Remote Config instance.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(LogUtil.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);


        // Set default Remote Config values. In general you should have in app defaults for all
        // values that you may configure using Remote Config later on. The idea is that you
        // use the in app defaults and when you need to adjust those defaults, you set an updated
        // value in the App Manager console. Then the next time you application fetches from the
        // server, the updated value will be used. You can set defaults via an xml file like done
        // here or you can set defaults inline by using one of the other setDefaults methods.
        mFirebaseRemoteConfig.setDefaults(getDefaultLocalConfigs());
    }

    private HashMap<String, Object> getDefaultLocalConfigs() {
        HashMap<String, Object> defaults = new HashMap<>();
        /* Normal Config Infos */
        defaults.put(Keys.CONFIG_VERSION, GlobalPref.getIns().getRemoteConfigVersion());
        /* App Version Infos */
        defaults.put(Keys.APP_VERSION_LATEST, GlobalPref.getIns().getRemoteConfigAppVersionLatest());
        defaults.put(Keys.APP_VERSION_MIN, GlobalPref.getIns().getRemoteConfigAppVersionMin());
        return defaults;
    }


    /**
     * Fetch Version from server.
     **/
    public void fetchConfigs(final IFetchListener listener) {
        final CountingTimer mTimeoutTimer = new CountingTimer(TIME_OUT, new CountingTimer.ICountingListener() {
            @Override
            public void onTimeTickStart() {
            }

            @Override
            public void onTimeTick(long millis) {
            }

            @Override
            public void onTimeTickEnd() {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "fetching timeout");
                }
                if (listener != null) {
                    listener.onFetchComplete();
                }
            }

            @Override
            public void onInterrupted(long stopMillis) {
            }
        });
        // cacheExpirationSeconds is set to cacheExpiration here, indicating that any previously
        // fetched and cached config would be considered expired because it would have been fetched
        // more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
        // throttling is in progress. The default expiration duration is 43200 (12 hours).
        mTimeoutTimer.start();
        mFirebaseRemoteConfig.fetch(CACHE_EXPIRATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mTimeoutTimer.stop();
                final boolean isSuccess = task.isSuccessful();
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("onComplete, isSuccess: %s", isSuccess));
                }
                if (isSuccess) {
                    // Once the config is successfully fetched it must be activated before newly fetched
                    // values are returned.
                    mFirebaseRemoteConfig.activateFetched();
                }
                saveConfigs();

                if (listener != null) {
                    listener.onFetchComplete();
                }
            }
        });
    }

    private void saveConfigs() {
        saveNormalConfigInfos();
        saveAppVersionInfos();
    }

    /* Normal Config Infos */
    private void saveNormalConfigInfos() {
        long configVersion = mFirebaseRemoteConfig.getLong(Keys.CONFIG_VERSION);

        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "[Normal] configVersion: " + configVersion);
        }

        GlobalPref.getIns().saveRemoteConfigConfigVersion(configVersion);
    }

    /* App Version Infos */
    private void saveAppVersionInfos() {
        long appVersionLatest = mFirebaseRemoteConfig.getLong(Keys.APP_VERSION_LATEST);
        long appVersionMin = mFirebaseRemoteConfig.getLong(Keys.APP_VERSION_MIN);

        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("[AppVersion] appVersionLatest: %s, appVersionMin: %s", appVersionLatest, appVersionMin));
        }

        GlobalPref.getIns().saveRemoteConfigAppVersionLatest(appVersionLatest);
        GlobalPref.getIns().saveRemoteConfigAppVersionMin(appVersionMin);
    }

    public interface IFetchListener {
        void onFetchComplete();
    }
}