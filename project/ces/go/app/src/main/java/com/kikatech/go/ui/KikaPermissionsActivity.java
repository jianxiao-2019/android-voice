package com.kikatech.go.ui;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;

import com.kikatech.go.R;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.notification.NotificationListenerUtil;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.OverlayUtil;
import com.kikatech.go.util.PermissionUtil;
import com.kikatech.voice.util.AsyncThread;
import com.kikatech.voice.util.contact.ContactManager;

import static com.kikatech.go.util.PermissionUtil.PERMISSION_REQUEST;

/**
 * @author SkeeterWang Created on 2017/11/24.
 */

public class KikaPermissionsActivity extends BaseActivity {
    private static final String TAG = "KikaPermissionsActivity";

    private CheckBox mBtnAccessibility;
    private CheckBox mBtnNotificationListener;
    private CheckBox mBtnOverlay;
    private CheckBox mBtnKikaAllPermission;


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
        adjustPermissionAndBtnLayout();
    }

    private void bindView() {
        mBtnAccessibility = (CheckBox) findViewById(R.id.btn_permission_accessibility);
        mBtnNotificationListener = (CheckBox) findViewById(R.id.btn_permission_notification_listener);
        mBtnOverlay = (CheckBox) findViewById(R.id.btn_permission_overlay);
        mBtnKikaAllPermission = (CheckBox) findViewById(R.id.btn_permission_kika_all);
        bindListener();
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
    }


    private void adjustPermissionAndBtnLayout() {
        boolean hasPermissionAccessibility = AccessibilityUtils.isSettingsOn(KikaPermissionsActivity.this);
        mBtnAccessibility.setChecked(hasPermissionAccessibility);
        mBtnAccessibility.setEnabled(!hasPermissionAccessibility);
        boolean hasPermissionNL = NotificationListenerUtil.isPermissionNLEnabled(KikaPermissionsActivity.this);
        mBtnNotificationListener.setChecked(hasPermissionNL);
        mBtnNotificationListener.setEnabled(!hasPermissionNL);
        boolean hasPermissionOverlay = DeviceUtil.overM() && OverlayUtil.isPermissionOverlayEnabled(KikaPermissionsActivity.this);
        mBtnOverlay.setChecked(hasPermissionOverlay);
        mBtnOverlay.setEnabled(!hasPermissionOverlay);
        boolean hasAllKikaPermissions = PermissionUtil.hasAllKikaPermissions(KikaPermissionsActivity.this);
        mBtnKikaAllPermission.setChecked(hasAllKikaPermissions);
        mBtnKikaAllPermission.setEnabled(!hasAllKikaPermissions);
        boolean canStart = hasPermissionAccessibility && hasPermissionNL && hasPermissionOverlay && hasAllKikaPermissions;
        if (canStart) {
            showToast(getString(R.string.permission_toast));
            AsyncThread.getIns().executeDelay(new Runnable() {
                @Override
                public void run() {
                    DialogFlowForegroundService.processStart(KikaPermissionsActivity.this, DialogFlowForegroundService.class);
                    startAnotherActivity(KikaAlphaUiActivity.class, true, R.anim.activity_no_anim, R.anim.activity_slide_out_left);
                }
            }, 1500);
        }
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
