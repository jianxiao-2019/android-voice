package com.kikatech.go.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.notification.NotificationListenerUtil;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.OverlayUtil;
import com.kikatech.go.util.PermissionUtil;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * @author SkeeterWang Created on 2017/11/24.
 */

public class KikaLaunchActivity extends BaseActivity {
    private static final String TAG = "KikaLaunchActivity";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
