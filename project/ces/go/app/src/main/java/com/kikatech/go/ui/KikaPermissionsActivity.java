package com.kikatech.go.ui;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kikatech.go.R;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.notification.NotificationListenerUtil;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.OverlayUtil;
import com.kikatech.go.util.PermissionUtil;
import com.kikatech.voice.util.contact.ContactManager;

import static com.kikatech.go.util.PermissionUtil.PERMISSION_REQUEST;

/**
 * @author SkeeterWang Created on 2017/11/24.
 */

public class KikaPermissionsActivity extends BaseActivity {
    private static final String TAG = "KikaPermissionsActivity";

    private View mBtnAccessibility;
    private View mBtnNotificationListener;
    private View mBtnOverlay;
    private View mBtnKikaAllPermission;
    private View mBtnStart;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kika_permissions);
        bindView();

        if (PermissionUtil.hasPermissions(this, PermissionUtil.Permission.READ_CONTACTS)) {
            ContactManager.getIns().init(this);
        }
        if (PermissionUtil.hasPermissionLocation(this)) {
            LocationMgr.init(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adjustBtnLayout();
    }

    private void bindView() {
        mBtnAccessibility = findViewById(R.id.btn_permission_accessibility);
        mBtnNotificationListener = findViewById(R.id.btn_permission_notification_listener);
        mBtnOverlay = findViewById(R.id.btn_permission_overlay);
        mBtnKikaAllPermission = findViewById(R.id.btn_permission_kika_all);
        mBtnStart = findViewById(R.id.btn_permission_start);
        bindListener();
        adjustBtnLayout();
    }

    private void bindListener() {
        mBtnAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccessibilityUtils.openAccessibilitySettings(KikaPermissionsActivity.this);
            }
        });
        mBtnNotificationListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationListenerUtil.openSystemSettingsNLPage(KikaPermissionsActivity.this);
            }
        });
        mBtnOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DeviceUtil.overM()) {
                    OverlayUtil.openSystemSettingsOverlayPage(KikaPermissionsActivity.this);
                }
            }
        });
        mBtnKikaAllPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtil.checkAllKikaPermissions(KikaPermissionsActivity.this);
            }
        });
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnotherActivity(KikaAlphaUiActivity.class, true);
            }
        });
    }

    private void adjustBtnLayout() {
        boolean hasPermissionAccessibility = AccessibilityUtils.isSettingsOn(KikaPermissionsActivity.this);
        mBtnAccessibility.setEnabled(!hasPermissionAccessibility);
        boolean hasPermissionNL = NotificationListenerUtil.isPermissionNLEnabled(KikaPermissionsActivity.this);
        mBtnNotificationListener.setEnabled(!hasPermissionNL);
        boolean hasPermissionOverlay = DeviceUtil.overM() && OverlayUtil.isPermissionOverlayEnabled(KikaPermissionsActivity.this);
        mBtnOverlay.setEnabled(!hasPermissionOverlay);
        boolean hasAllKikaPermissions = PermissionUtil.hasAllKikaPermissions(KikaPermissionsActivity.this);
        mBtnKikaAllPermission.setEnabled(!hasAllKikaPermissions);
        boolean canStart = hasPermissionAccessibility && hasPermissionNL && hasPermissionOverlay && hasAllKikaPermissions;
        mBtnStart.setEnabled(canStart);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        LogUtil.log(TAG, "onRequestPermissionsResult, requestCode:" + requestCode);

        if (requestCode == PERMISSION_REQUEST) {
            for (String permission : permissions) {
                if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                    ContactManager.getIns().init(this);
                }
                if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                        || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    LocationMgr.init(this);
                }
            }
        }
    }
}
