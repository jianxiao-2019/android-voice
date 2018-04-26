package com.kikatech.go.util;

import android.content.Context;
import android.support.annotation.IntDef;

import com.kikatech.go.BuildConfig;
import com.kikatech.go.util.preference.GlobalPref;

/**
 * @author SkeeterWang Created on 2018/4/20.
 */

public class VersionControlUtil {
    private static final String TAG = "VersionControlUtil";

    private static final String UPDATE_APP_URL_MAIN = "https://drive.google.com/open?id=1gU6In8OUPRxd-uz1KU-rXV3nLMrFklfy";
    private static final String UPDATE_APP_URL_MANUFACTURER = "https://drive.google.com/file/d/1A7aZ9s7P5C31TulG2BqbaQ1kG75E_JKZ/view?usp=drivesdk";
    private static final String UPDATE_APP_URL = FlavorUtil.isFlavorManufacturer() ? UPDATE_APP_URL_MANUFACTURER : UPDATE_APP_URL_MAIN;

    private static final int APP_VERSION_STATUS_LATEST = 0;
    private static final int APP_VERSION_STATUS_UPDATE = 1;
    private static final int APP_VERSION_STATUS_BLOCK = 2;

    @IntDef({APP_VERSION_STATUS_LATEST, APP_VERSION_STATUS_UPDATE, APP_VERSION_STATUS_BLOCK})
    public @interface AppVersionStatus {
        int LATEST = APP_VERSION_STATUS_LATEST;
        int UPDATE = APP_VERSION_STATUS_UPDATE;
        int BLOCK = APP_VERSION_STATUS_BLOCK;
    }

    @AppVersionStatus
    public static int checkAppVersion() {
        long currentVersion = BuildConfig.VERSION_CODE;
        long latestVersion = GlobalPref.getIns().getRemoteConfigAppVersionLatest();
        long minVersion = GlobalPref.getIns().getRemoteConfigAppVersionMin();

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("current: %s, latest: %s, min: %s", currentVersion, latestVersion, minVersion));
        }

        if (currentVersion <= minVersion) {
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, "status: BLOCK");
            }
            return AppVersionStatus.BLOCK;
        } else if (currentVersion >= latestVersion) {
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, "status: LATEST");
            }
            return AppVersionStatus.LATEST;
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, "status: UPDATE");
            }
            return AppVersionStatus.UPDATE;
        }
    }

    public static void openUpdatePage(Context context) {
        IntentUtil.openBrowser(context, UPDATE_APP_URL);
    }
}
