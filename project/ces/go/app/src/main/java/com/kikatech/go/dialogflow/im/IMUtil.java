package com.kikatech.go.dialogflow.im;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.im.send.IMContent;
import com.kikatech.go.util.AppConstants;
import com.kikatech.go.util.AppUtil;
import com.kikatech.go.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class IMUtil {

    public static final String DF_ENTIY_IM_APP_LINE = "line";
    public static final String DF_ENTIY_IM_APP_WHATS_APP = "what's app";
    public static final String DF_ENTIY_IM_APP_FB_MESSENGER = "facebook messenger";
    public static final String DF_ENTIY_IM_APP_WECHAT = "wechat";

    private static final String KEY_ANY = "any";
    private static final String KEY_NAME = "given-name";
    private static final String KEY_IM_APP = "im-app";

    public static final String KEY_SWITCH_SCENE_NAME = "user_name";

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
                return checkPackageAvailability(pkgName);
            }
        }
        return false;
    }

    private static boolean checkPackageAvailability(String pkgName) {
        // Check if app is installed and enabled
        return true;
    }

    public static String prepareSwitchSceneInfo(IMContent sc) {
        if (sc == null || TextUtils.isEmpty(sc.getSendTarget())) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_SWITCH_SCENE_NAME, sc.getSendTarget());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (LogUtil.DEBUG) LogUtil.log("IMUtil", "prepareSwitchSceneInfo:" + json.toString());
        return json.toString();
    }
}