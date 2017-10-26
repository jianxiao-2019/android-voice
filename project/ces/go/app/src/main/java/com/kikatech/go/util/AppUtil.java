package com.kikatech.go.util;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * @author jasonli Created on 2017/10/26.
 */

public class AppUtil {

    public static boolean isAppInstalled(Context context, AppInfo appInfo) {
        return isAppInstalled(context, appInfo.getPackageName());
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignore) {}
        return false;
    }
}
