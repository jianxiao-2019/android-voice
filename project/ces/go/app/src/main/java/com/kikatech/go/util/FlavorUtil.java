package com.kikatech.go.util;

import com.kikatech.go.BuildConfig;

/**
 * @author SkeeterWang Created on 2018/2/1.
 */

public class FlavorUtil {
    private static final String TAG = "FlavorUtil";

    public static final String FLAVOR_MAIN = "main";
    public static final String FLAVOR_MANUFACTURER = "manufacturer";

    public static String getFlavor() {
        return BuildConfig.FLAVOR;
    }

    public static boolean isFlavorManufacturer() {
        return FLAVOR_MANUFACTURER.equals(getFlavor());
    }

    public static void print() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("flavor: %s", getFlavor()));
        }
    }
}
