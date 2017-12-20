package com.kikatech.go.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class PackageManagerUtil {
    private static final String TAG = "PackageManagerUtil";

    public static String getAppVersionName(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return null;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return false;
    }

    public static boolean isAppInstalledAndEnabled(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).enabled;
        } catch (Exception ignore) {
            // possible PackageManager.NameNotFoundException
        }
        return false;
    }

    public static String getAppName(Context context, String packageName) {
        String applicationName = "(unknown)";
        try {
            final PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (final PackageManager.NameNotFoundException ignore) {
        }
        return applicationName;
    }

    public static int getAppUid(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).uid;
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return 0;
    }
}
