package com.kikatech.go.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.animation.AlphaAnimation;

import com.kikatech.go.R;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.notification.NotificationListenerUtil;
import com.kikatech.go.util.AsyncThread;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.OverlayUtil;
import com.kikatech.go.util.PermissionUtil;

/**
 * @author SkeeterWang Created on 2017/11/24.
 */

public class KikaLaunchActivity extends BaseActivity {
    private static final String TAG = "KikaLaunchActivity";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_launch);

        animate();

        AsyncThread.getIns().executeDelay(new Runnable() {
            @Override
            public void run() {
                determinePageToGo();
            }
        }, 1800);
    }

    private void animate() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(1000);
        findViewById(R.id.launch_page_logo).startAnimation(alphaAnimation);
        findViewById(R.id.launch_page_slogan).startAnimation(alphaAnimation);
    }

    private void determinePageToGo() {
        Context context = KikaLaunchActivity.this;
        if (!AccessibilityUtils.isSettingsOn(context)
                || !NotificationListenerUtil.isPermissionNLEnabled(context)
                || (DeviceUtil.overM() && !OverlayUtil.isPermissionOverlayEnabled(context))
                || !PermissionUtil.hasAllKikaPermissions(context)) {
            startAnotherActivity(KikaPermissionsActivity.class, true);
        } else {
            startAnotherActivity(KikaAlphaUiActivity.class, true);
        }
    }
}
