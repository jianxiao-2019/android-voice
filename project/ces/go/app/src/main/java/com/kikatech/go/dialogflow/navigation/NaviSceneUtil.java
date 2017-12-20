package com.kikatech.go.dialogflow.navigation;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.navigation.NavigationManager;
import com.kikatech.go.navigation.provider.BaseNavigationProvider;
import com.kikatech.go.ui.KikaAlphaUiActivity;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by bradchang on 2017/11/14.
 */

public class NaviSceneUtil {

    private final static String PRM_ADDRESS = "address";
    private final static String PRM_LOCATION = "location";
    private final static String PRM_ATTRACTION_US = "place-attraction-us";
    private final static String PRM_SPECIFIC_LOC = "specific-loc";
    private final static String PRM_CITY = "city";
    private final static String PRM_SUBADMIN_AREA = "subadmin-area";
    private final static String PRM_ADMIN_AREA = "admin-area";
    private final static String PRM_ZIP_CODE = "zip-code";

    private final static String KEY_STREET_ADDR = "street-address";
    private final static String KEY_BUSINESS_NAME = "business-name";
    private final static String KEY_SHORTCUT = "shortcut";

    private final static String[] PRM_ARRAY = {PRM_CITY, PRM_LOCATION, PRM_ADDRESS, PRM_ATTRACTION_US, PRM_SPECIFIC_LOC, PRM_SUBADMIN_AREA, PRM_ZIP_CODE, KEY_BUSINESS_NAME};
    private final static String[] LOC_KEYS = {PRM_CITY, KEY_STREET_ADDR, KEY_BUSINESS_NAME, KEY_SHORTCUT, PRM_SUBADMIN_AREA, PRM_ADMIN_AREA, PRM_ZIP_CODE};

    private static boolean sNavigating = false;

    public static String parseAddress(@NonNull Bundle parm) {
        if (LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "parm : " + parm);
        StringBuilder fullAddress = new StringBuilder();
        for (String key : PRM_ARRAY) {
            String addr = parm.getString(key);
            if (!TextUtils.isEmpty(addr)) {
                String location = "";
                boolean parseSuccess = false;
                boolean validJson = true;
                try {
                    JSONObject json = new JSONObject(addr);
                    for (String locKey : LOC_KEYS) {
                        if (json.has(locKey)) {
                            location = json.getString(locKey);
                            if (!TextUtils.isEmpty(location)) {
                                fullAddress.append(location).append(" ");
                                parseSuccess = true;
                            }
                        }
                    }
                } catch (JSONException ignored) {
                    validJson = false;
                }
                if (!parseSuccess && !validJson) {
                    fullAddress.append(addr.replace("\"", ""));
                }
                if (LogUtil.DEBUG)
                    LogUtil.log("NaviSceneUtil", "parseAddress : " + fullAddress.toString());
            }
        }
        if (LogUtil.DEBUG)
            LogUtil.log("NaviSceneUtil", "parseAddress : " + fullAddress.toString() + ", parm:" + parm.keySet().size());
        return StringUtil.upperCaseFirstWord(fullAddress.toString());
    }

    public synchronized static void navigateToLocation(Context ctx, String loc) {
        if (LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "navigateToLocation:" + loc);
        ArrayList<BaseNavigationProvider.NavigationAvoid> avoidList = new ArrayList<>();
        final BaseNavigationProvider.NavigationAvoid[] avoids = avoidList.toArray(new BaseNavigationProvider.NavigationAvoid[0]);
        NavigationManager.getIns().startNavigation(ctx, loc, BaseNavigationProvider.NavigationMode.DRIVE, avoids);
        sNavigating = true;
    }

    /**
     * This is a workaround ...
     */
    public synchronized static void stopNavigation(final Context ctx, final Class<?> targetClz) {
        if (!sNavigating) {
            if (LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "Not navigating, ignore command");
            return;
        }

        if (LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "Start to stop navigation ..., targetClz:" + targetClz);

        final Handler uiHandler = new Handler(Looper.getMainLooper());

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                sNavigating = false;
                NavigationManager.getIns().stopNavigation(ctx, new NavigationManager.INavigationCallback() {
                    @Override
                    public void onStop() {
                        if (targetClz == null) {
                            return;
                        }
                        uiHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    android.content.Intent intent = new android.content.Intent(ctx, targetClz);
                                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    pendingIntent.send();
                                } catch (Exception ignore) {
                                }
                            }
                        }, 3000);
                    }
                });
            }
        });
    }
}