package com.kikatech.go.notification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/10/25.
 */

public class NotificationListenerUtil {
    private static final String TAG = "NotificationListenerUtil";

    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    public static boolean openSystemSettingsNLPage(Activity activity) {
        try {
            activity.startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (Exception ignore) {
            return false;
        }
        return true;
    }

    public static boolean isPermissionNLEnabled(Context context) {
        try {
            Context appCtx = context.getApplicationContext();
            final String flat = Settings.Secure.getString(appCtx.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
            boolean isEnabled = !TextUtils.isEmpty(flat) && flat.contains(appCtx.getPackageName());
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "isPermissionNLEnabled ? " + isEnabled);
            }
            return isEnabled;
        } catch (Exception ignore) {
            return false;
        }
    }
}
