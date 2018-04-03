package com.kikatech.go.ui;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;

import com.kikatech.go.R;
import com.kikatech.go.accessibility.AccessibilityUtils;
import com.kikatech.go.navigation.location.LocationMgr;
import com.kikatech.go.notification.NotificationListenerUtil;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.AsyncThreadPool;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.IntentUtil;
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

    private static final int CHECK_NOTHING = 0;
    private static final int CHECK_PERMISSION_OVERLAY = 1;
    private static final int CHECK_PERMISSION_ACCESSIBILITY = 2;
    private static final int CHECK_PERMISSION_NL = 3;
    private static final int CHECK_DONE = 4;

    @IntDef({CHECK_NOTHING, CHECK_PERMISSION_OVERLAY, CHECK_PERMISSION_ACCESSIBILITY, CHECK_PERMISSION_NL, CHECK_DONE})
    private @interface PermissionCheckState {
        int IDLE = CHECK_NOTHING;
        int CHECKING_OVERLAY = CHECK_PERMISSION_OVERLAY;
        int CHECKING_ACCESSIBILITY = CHECK_PERMISSION_ACCESSIBILITY;
        int CHECKING_NL = CHECK_PERMISSION_NL;
        int DONE = CHECK_DONE;
    }


    private CheckBox mBtnOverlay;
    private CheckBox mBtnAccessibility;
    private CheckBox mBtnNotificationListener;
    private CheckBox mBtnKikaAllPermission;

    @PermissionCheckState
    private int mCurrentState = PermissionCheckState.IDLE;

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

        AsyncThreadPool.getIns().execute(pollingRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adjustPermissionAndBtnLayout();
        mCurrentState = PermissionCheckState.IDLE;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AsyncThreadPool.getIns().remove(pollingRunnable);
    }

    private void bindView() {
        mBtnOverlay = (CheckBox) findViewById(R.id.btn_permission_overlay);
        mBtnAccessibility = (CheckBox) findViewById(R.id.btn_permission_accessibility);
        mBtnNotificationListener = (CheckBox) findViewById(R.id.btn_permission_notification_listener);
        mBtnKikaAllPermission = (CheckBox) findViewById(R.id.btn_permission_kika_all);
        bindListener();
    }

    private void bindListener() {
        mBtnOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DeviceUtil.overM()) {
                    mCurrentState = PermissionCheckState.CHECKING_OVERLAY;
                    OverlayUtil.openSystemSettingsOverlayPage(KikaPermissionsActivity.this);
                }
            }
        });
        mBtnAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentState = PermissionCheckState.CHECKING_ACCESSIBILITY;
                AccessibilityUtils.openAccessibilitySettings(KikaPermissionsActivity.this);
            }
        });
        mBtnNotificationListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentState = PermissionCheckState.CHECKING_NL;
                NotificationListenerUtil.openSystemSettingsNLPage(KikaPermissionsActivity.this);
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
        boolean hasPermissionOverlay = DeviceUtil.overM() && OverlayUtil.isPermissionOverlayEnabled(KikaPermissionsActivity.this);
        mBtnOverlay.setChecked(hasPermissionOverlay);
        mBtnOverlay.setEnabled(!hasPermissionOverlay);
        boolean hasPermissionAccessibility = AccessibilityUtils.isSettingsOn(KikaPermissionsActivity.this);
        mBtnAccessibility.setChecked(hasPermissionAccessibility);
        mBtnAccessibility.setEnabled(!hasPermissionAccessibility);
        boolean hasPermissionNL = NotificationListenerUtil.isPermissionNLEnabled(KikaPermissionsActivity.this);
        mBtnNotificationListener.setChecked(hasPermissionNL);
        mBtnNotificationListener.setEnabled(!hasPermissionNL);
        boolean hasAllKikaPermissions = PermissionUtil.hasAllKikaPermissions(KikaPermissionsActivity.this);
        mBtnKikaAllPermission.setChecked(hasAllKikaPermissions);
        mBtnKikaAllPermission.setEnabled(!hasAllKikaPermissions);
        boolean canStart = hasPermissionAccessibility && hasPermissionNL && hasPermissionOverlay && hasAllKikaPermissions;
        if (canStart) {
            showToast(getString(R.string.permission_toast));
            AsyncThreadPool.getIns().executeDelay(new Runnable() {
                @Override
                public void run() {
                    DialogFlowForegroundService.processStart(KikaPermissionsActivity.this, DialogFlowForegroundService.class);
                    startAnotherActivity(KikaAlphaUiActivity.class, true, R.anim.activity_no_anim, R.anim.activity_slide_out_left);
                }
            }, 800);
        }
    }


    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            checkPermissions();
            AsyncThreadPool.getIns().executeDelay(this, 500);
        }

        private void checkPermissions() {
            boolean isAppForeground = KikaMultiDexApplication.isApplicationInForeground();
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, String.format("isAppForeground: %s", isAppForeground));
            }
            switch (mCurrentState) {
                case PermissionCheckState.IDLE:
                    if (LogUtil.DEBUG) {
                        LogUtil.logd(TAG, "IDLE");
                    }
                    break;
                case PermissionCheckState.CHECKING_OVERLAY:
                    boolean hasPermissionOverlay = DeviceUtil.overM() && OverlayUtil.isPermissionOverlayEnabled(KikaPermissionsActivity.this);
                    if (LogUtil.DEBUG) {
                        LogUtil.logd(TAG, String.format("Overlay permission: %s", hasPermissionOverlay));
                    }
                    if (!isAppForeground && hasPermissionOverlay) {
                        IntentUtil.openPermissionPage(KikaPermissionsActivity.this);
                    }
                    break;
                case PermissionCheckState.CHECKING_ACCESSIBILITY:
                    boolean hasPermissionAccessibility = AccessibilityUtils.isSettingsOn(KikaPermissionsActivity.this);
                    if (LogUtil.DEBUG) {
                        LogUtil.logd(TAG, String.format("Accessibility permission: %s", hasPermissionAccessibility));
                    }
                    if (!isAppForeground && hasPermissionAccessibility) {
                        IntentUtil.openPermissionPage(KikaPermissionsActivity.this);
                    }
                    break;
                case PermissionCheckState.CHECKING_NL:
                    boolean hasPermissionNL = NotificationListenerUtil.isPermissionNLEnabled(KikaPermissionsActivity.this);
                    if (LogUtil.DEBUG) {
                        LogUtil.logd(TAG, String.format("NL permission: %s", hasPermissionNL));
                    }
                    if (!isAppForeground && hasPermissionNL) {
                        IntentUtil.openPermissionPage(KikaPermissionsActivity.this);
                    }
                    break;
                case PermissionCheckState.DONE:
                    break;
            }
        }
//        }
    };

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
