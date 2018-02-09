package com.kikatech.go.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;

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
        determinePageToGo();
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

    private void determinePageToGo() {
        Context context = KikaLaunchActivity.this;
        if (GlobalPref.getIns().getIsFirstLaunch()) {
            AsyncThreadPool.getIns().executeDelay(new Runnable() {
                @Override
                public void run() {
                    startAnotherActivity(KikaFeatureHighlightActivity.class, true);
                }
            }, TIME_OUT);
        } else if (!AccessibilityUtils.isSettingsOn(context)
                || !NotificationListenerUtil.isPermissionNLEnabled(context)
                || (DeviceUtil.overM() && !OverlayUtil.isPermissionOverlayEnabled(context))
                || !PermissionUtil.hasAllKikaPermissions(context)) {
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
    }
}
