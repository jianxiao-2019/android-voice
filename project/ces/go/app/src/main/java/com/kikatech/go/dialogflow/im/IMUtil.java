package com.kikatech.go.dialogflow.im;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.AppUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class IMUtil {

    static final String DF_ENTIY_IM_APP_LINE = "line";
    static final String DF_ENTIY_IM_APP_WHATS_APP = "what's app";
    static final String DF_ENTIY_IM_APP_FB_MESSENGER = "facebook messenger";
    static final String DF_ENTIY_IM_APP_WECHAT = "wechat";

    private static final String KEY_ANY = "any";
    private static final String KEY_NAME = "given-name";
    private static final String KEY_IM_APP = "im-app";

    // TODO should bind settings
    private static final String[] SUPPORTED_IM = {AppConstants.PACKAGE_WHATSAPP};//, AppConstants.PACKAGE_MESSENGER};

    private static String getBundleString(@NonNull Bundle parm, String key) {
        return parm.getString(key, "").replace("\"", "");
    }

    public static IMContent parse(@NonNull Bundle parm) {
        if (LogUtil.DEBUG) LogUtil.log("IMContent", "parm:" + parm);

        String imApp = parseIMApp(parm);
        String targetName = getBundleString(parm, KEY_NAME);
        String msgBody = getBundleString(parm, KEY_ANY);

        IMContent imc = new IMContent(imApp, targetName, msgBody);
        if (LogUtil.DEBUG) LogUtil.log("IMContent", "IMContent:" + imc);

        return imc;
    }

    private static String parseIMApp(@NonNull Bundle parm) {
        String imApp = parm.getString(KEY_IM_APP, "");
        if (!TextUtils.isEmpty(imApp)) {
            try {
                JSONArray appList = new JSONArray(imApp);
                if (appList.length() > 0) {
                    return appList.getString(0);
                }
            } catch (JSONException ignored) {
            }
            return imApp.replace("\"", "");
        }
        return "";
    }

    public static boolean isIMAppSupported(Context ctx, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }

        if (!AppUtil.isAppInstalled(ctx, pkgName)) {
            if (LogUtil.DEBUG) LogUtil.log("IMUtil", pkgName + " is not installed !!");
            return false;
        }

        // Check if package is supported
        for (String s : SUPPORTED_IM) {
            if (s.equals(pkgName)) {
                return checkPackageAvaibility(pkgName);
            }
        }
        return false;
    }

    private static boolean checkPackageAvaibility(String pkgName) {
        // Check if app is installed and enabled
        return true;
    }
}