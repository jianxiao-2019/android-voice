package com.kikatech.go.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kikatech.go.R;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.notification.NotificationListenerUtil;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.AsyncThreadPool;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.OverlayUtil;
import com.kikatech.go.util.PermissionUtil;
import com.kikatech.go.util.VersionControlUtil;
import com.kikatech.go.util.firebase.RemoteConfigUtil;
import com.kikatech.go.util.preference.GlobalPref;
import com.kikatech.go.util.timer.CountingTimer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author SkeeterWang Created on 2017/11/24.
 */

public class KikaLaunchActivity extends BaseActivity {
    private static final String TAG = "KikaLaunchActivity";

    private static final long TIME_OUT = 1200;


    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event from {@link com.kikatech.go.services.DialogFlowForegroundService}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceEvent(DFServiceEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT:
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "ACTION_ON_DIALOG_FLOW_INIT");
                }
                if (mTimeoutTimer.isCounting()) {
                    mTimeoutTimer.stop();
                }
                break;
        }
    }


    private CountingTimer mTimeoutTimer = new CountingTimer(TIME_OUT, new CountingTimer.ICountingListener() {
        @Override
        public void onTimeTickStart() {
        }

        @Override
        public void onTimeTick(long millis) {
        }

        @Override
        public void onTimeTickEnd() {
            startAnotherActivity(KikaAlphaUiActivity.class, true);
        }

        @Override
        public void onInterrupted(long stopMillis) {
            startAnotherActivity(KikaAlphaUiActivity.class, true);
        }
    });


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_launch);
        animate();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // #NOTICE issue:
        // https://stackoverflow.com/questions/37501124/firebaseremoteconfig-fetch-does-not-trigger-oncompletelistener-every-time
        performPreload();
    }

    @Override
    protected void onDestroy() {
        unregisterReceivers();
        super.onDestroy();
    }

    private void registerReceivers() {
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private void unregisterReceivers() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ignore) {
        }
    }

    private void animate() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(1000);
        findViewById(R.id.launch_page_logo).startAnimation(alphaAnimation);
        findViewById(R.id.launch_page_slogan).startAnimation(alphaAnimation);
    }

    private void performPreload() {
        boolean isGooglePlayServicesAvailable = checkGooglePlayServices();
        if (isGooglePlayServicesAvailable) {
            fetchRemoteConfig();
        } else {
            determinePageToGo();
        }
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (LogUtil.DEBUG) LogUtil.logd(TAG, googleApiAvailability.getErrorString(status));
        switch (status) {
            case ConnectionResult.SUCCESS:
                // google play services is updated.
                // your code goes here...
                return true;
            default:
                // ask user to update google play services.
                return false;
        }
    }

    private void fetchRemoteConfig() {
        RemoteConfigUtil.getIns().fetchConfigs(new RemoteConfigUtil.IFetchListener() {
            @Override
            public void onFetchComplete() {
                determinePageToGo();
            }
        });
    }

    private void determinePageToGo() {
        @VersionControlUtil.AppVersionStatus int status = VersionControlUtil.checkAppVersion();
        switch (status) {
            case VersionControlUtil.AppVersionStatus.BLOCK:
                startAnotherActivity(KikaBlockActivity.class, true);
                break;
            case VersionControlUtil.AppVersionStatus.UPDATE:
            case VersionControlUtil.AppVersionStatus.LATEST:
                if (GlobalPref.getIns().getIsFirstLaunch()) {
                    AsyncThreadPool.getIns().executeDelay(new Runnable() {
                        @Override
                        public void run() {
                            startAnotherActivity(KikaFeatureHighlightActivity.class, true);
                        }
                    }, TIME_OUT);
                } else if (!AccessibilityUtils.isSettingsOn(KikaLaunchActivity.this)
                        || !NotificationListenerUtil.isPermissionNLEnabled(KikaLaunchActivity.this)
                        || (DeviceUtil.overM() && !OverlayUtil.isPermissionOverlayEnabled(KikaLaunchActivity.this))
                        || !PermissionUtil.hasAllKikaPermissions(KikaLaunchActivity.this)) {
                    AsyncThreadPool.getIns().executeDelay(new Runnable() {
                        @Override
                        public void run() {
                            startAnotherActivity(KikaPermissionsActivity.class, true);
                        }
                    }, TIME_OUT);
                } else {
                    registerReceivers();
                    DialogFlowForegroundService.processStart(KikaLaunchActivity.this, DialogFlowForegroundService.class);
                    mTimeoutTimer.start();
                }
                break;
        }
    }
}