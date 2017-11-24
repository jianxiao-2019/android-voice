package com.kikatech.go.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import com.kikatech.go.BuildConfig;

/**
 * @author SkeeterWang Created on 2017/10/30.
 */

public class OverlayUtil {
    private static final String TAG = "OverlayUtil";

    private static final String SETTINGS_OVERLAY_PACKAGE_NAME = "package:" + BuildConfig.APPLICATION_ID;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void openSystemSettingsOverlayPage(Activity activity) {
        try {
            Intent settingIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            settingIntent.setData(Uri.parse(SETTINGS_OVERLAY_PACKAGE_NAME));
            settingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(settingIntent);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * NOTICE: call this on UI Thread
     * - issue: https://code.google.com/p/android/issues/detail?id=227835&sort=-opened&colspec=ID%20Status%20Priority%20Owner%20Summary%20Stars%20Reporter%20Opened
     * - worked around: https://code.google.com/p/android/issues/detail?id=196372#c15
     *
     * @return has crashed ? true : try return system setting
     **/
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isPermissionOverlayEnabled(Context context) {
        try {
            boolean isEnabled = Settings.canDrawOverlays(context);
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "isPermissionOverlayEnabled ? " + isEnabled);
            }
            return isEnabled;
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
            return false;
        }
    }
}
