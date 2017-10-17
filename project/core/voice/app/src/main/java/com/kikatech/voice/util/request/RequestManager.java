package com.kikatech.voice.util.request;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Created by momo on 1/21/16.
 */
public class RequestManager {

    public static String getSign(Context context) {
        final String duid = DeviceUtils.getUID(context);
        String original = String.format((Locale) null, "app_key%1$sapp_version%2$sduid%3$s"
                , "78472ddd7528bcacc15725a16aeec190", "20", duid);
        String md5String = MD5.getMD5(original);
//        if (LogUtils.verbose(TAG)) {
//            Log.v(TAG, String.format("sign original string %1$s %n%2$s", original, md5String));
//        }
        return md5String;
    }

    public static String generateUserAgent(Context context) {
        String country = Locale.getDefault().getCountry();
        if (!MiscUtil.isValidHeaderString(country)) {
            country = "US";
        }
        String language = Locale.getDefault().getLanguage();
        if (!MiscUtil.isValidHeaderString(language)) {
            language = "en";
        }
        final String duid = DeviceUtils.getUID(context);
        DisplayMetrics metric = Resources.getSystem().getDisplayMetrics();
        // （hdpi: 240 , ldpi: 120 , mdpi: 160 , xhdpi: 320）

        int dpi = metric.densityDpi;
//        if (BuildConfig.FUNC_PREINSTALL) {
//            return String.format(Locale.US,
//                    "%s/%s (%s/%s) Country/%s Language/%s System/android Version/%s Screen/%s channel/%s",
//                    BuildConfig.APPLICATION_ID, String.valueOf(BuildConfig.VERSION_CODE),
//                    duid, BuildConfig.AGENT_APPKEY, country, language,
//                    String.valueOf(Build.VERSION.SDK_INT), String.valueOf(dpi), BuildConfig.AGENT_CHANNEL);
//        } else {
            return String.format(Locale.US,
                    "%s/%s (%s/%s) Country/%s Language/%s System/android Version/%s Screen/%s",
                    "com.qisiemoji.inputmethod.debug", "20",
                    duid, "78472ddd7528bcacc15725a16aeec190", country, language,
                    String.valueOf(Build.VERSION.SDK_INT), String.valueOf(dpi));
//        }
    }
}
