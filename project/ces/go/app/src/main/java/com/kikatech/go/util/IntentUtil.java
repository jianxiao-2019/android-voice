package com.kikatech.go.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.kikatech.go.ui.activity.KikaGoActivity;
import com.kikatech.go.ui.activity.KikaPermissionsActivity;
import com.kikatech.go.ui.activity.KikaTutorialActivity;

/**
 * @author SkeeterWang Created on 2017/11/29.
 */

public class IntentUtil {
    private static final String TAG = "IntentUtil";

    private static final String KIKA_GO_PRODUCT_URL = "http://www.kika.ai/#home-section";

    public static boolean openKikaGo(Context context) {
        Intent intent = new Intent(context, KikaGoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return sendPendingIntent(context, intent);
    }

    public static boolean openKikaTutorial(Context context) {
        Intent intent = new Intent(context, KikaTutorialActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return sendPendingIntent(context, intent);
    }

    public static boolean openPermissionPage(Context context) {
        Intent intent = new Intent(context, KikaPermissionsActivity.class);
        return sendPendingIntent(context, intent);
    }

    public static boolean openKikaGoProductWeb(Context context) {
        return openBrowser(context, KIKA_GO_PRODUCT_URL);
    }

    public static boolean openBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        return sendPendingIntent(context, intent);
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
