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
import com.kikatech.go.util.LogUtil;

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
    private final static String[] PRM_ARRAY = {PRM_LOCATION, PRM_ADDRESS, PRM_ATTRACTION_US, PRM_SPECIFIC_LOC, PRM_CITY, PRM_SUBADMIN_AREA};

    private final static String KEY_STREET_ADDR = "street-address";
    private final static String KEY_BUSINESS_NAME = "business-name";
    private final static String KEY_SHORTCUT = "shortcut";
    private final static String[] LOC_KEYS = {KEY_STREET_ADDR, KEY_BUSINESS_NAME, KEY_SHORTCUT};

    private static boolean sNavigating = false;

    public static String parseAddress(@NonNull Bundle parm) {
        for (String key : PRM_ARRAY) {
            String addr = parm.getString(key);
            if (!TextUtils.isEmpty(addr)) {
                String location = "";
                try {
                    JSONObject json = new JSONObject(addr);
                    for (String locKey : LOC_KEYS) {
                        if (json.has(locKey)) location = json.getString(locKey);
                    }
                } catch (JSONException ignored) {
                }
                String addrResult = TextUtils.isEmpty(location) ? addr : location;
                if(LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "parseAddress : " + addrResult);
                return addrResult;
            }
        }
        if(LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "parseAddress : <empty>" + ", parm:" + parm.keySet().size());
        return "";
    }

    public synchronized static void navigateToLocation(Context ctx, String loc) {
        ArrayList<BaseNavigationProvider.NavigationAvoid> avoidList = new ArrayList<>();
        final BaseNavigationProvider.NavigationAvoid[] avoids = avoidList.toArray(new BaseNavigationProvider.NavigationAvoid[0]);
        NavigationManager.getIns().startNavigation(ctx, loc, BaseNavigationProvider.NavigationMode.DRIVE, avoids);
        sNavigating = true;
    }

    /**
     * This is a workaround ...
     */
    public synchronized static void stopNavigation(final Context ctx) {
        if(!sNavigating) {
            if(LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "Not navigating, ignore command");
            return;
        }

        if(LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "Start to stop navigation ...");

        sNavigating = false;
        NavigationManager.getIns().stopNavigation(ctx);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
//                try {
//                    android.content.Intent intent = new android.content.Intent(KikaDialogFlowActivity.this, KikaDialogFlowActivity.class);
//                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    PendingIntent pendingIntent = PendingIntent.getActivity(KikaDialogFlowActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    pendingIntent.send();
//                } catch (Exception ignore) {
//                }
            }
        }, 4000);
    }
}
