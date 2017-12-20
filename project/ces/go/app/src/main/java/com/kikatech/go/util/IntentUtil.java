package com.kikatech.go.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.kikatech.go.ui.KikaAlphaUiActivity;

/**
 * @author SkeeterWang Created on 2017/11/29.
 */

public class IntentUtil {
    private static final String TAG = "IntentUtil";

    public static boolean openKikaGo(Context context) {
        Intent intent = new Intent(context, KikaAlphaUiActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return IntentUtil.sendPendingIntent(context, intent);
    }

    /**
     * due to issue
     * <a href="http://stackoverflow.com/questions/5600084/starting-an-activity-from-a-service-after-home-button-pressed-without-the-5-seco">
     * http://stackoverflow.com/questions/5600084/starting-an-activity-from-a-service-after-home-button-pressed-without-the-5-seco
     * </a>
     **/
    public static boolean sendPendingIntent(Context context, Intent intent) {
        Context appCtx = context.getApplicationContext();
        boolean isSucceed = false;
        try {
            PendingIntent pendingIntent = PendingIntent.getActivity(appCtx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent.send();
            isSucceed = true;
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
            try {
                appCtx.startActivity(intent);
                isSucceed = true;
            } catch (Exception ignore) {
            }
        }
        return isSucceed;
    }
}
