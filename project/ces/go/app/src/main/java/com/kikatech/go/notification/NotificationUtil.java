package com.kikatech.go.notification;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * @author jasonli Created on 2017/10/26.
 */

public class NotificationUtil {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    public static boolean isPermissionNLEnabled(Context context) {
        try {
            Context appCtx = context.getApplicationContext();
            final String flat = Settings.Secure.getString(appCtx.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
            return !TextUtils.isEmpty(flat) && flat.contains(appCtx.getPackageName());
        } catch (Exception ignore) {
            return false;
        }
    }
}
