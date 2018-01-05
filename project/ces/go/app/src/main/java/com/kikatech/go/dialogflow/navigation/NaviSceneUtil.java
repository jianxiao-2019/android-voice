package com.kikatech.go.dialogflow.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.navigation.NavigationManager;
import com.kikatech.go.navigation.provider.BaseNavigationProvider;
import com.kikatech.go.util.AsyncThread;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;

import java.util.ArrayList;

/**
 * Created by bradchang on 2017/11/14.
 */

public class NaviSceneUtil {

    private final static String PARAM_DESTINATION = "destination";

    private static boolean sNavigating = false;

    public static String parseAddress(@NonNull Bundle params) {
        if (LogUtil.DEBUG) {
            LogUtil.log("NaviSceneUtil", "params : " + params);
        }
        String destination = params.getString(PARAM_DESTINATION);
        if (!TextUtils.isEmpty(destination)) {
            try {
                destination = destination.substring(1, destination.length() - 1);
            } catch (Exception ignore) {
            }
        }
        return destination;
    }

    public synchronized static void navigateToLocation(Context ctx, String loc) {
        if (LogUtil.DEBUG) {
            LogUtil.log("NaviSceneUtil", "navigateToLocation:" + loc);
        }
        ArrayList<BaseNavigationProvider.NavigationAvoid> avoidList = new ArrayList<>();
        final BaseNavigationProvider.NavigationAvoid[] avoids = avoidList.toArray(new BaseNavigationProvider.NavigationAvoid[0]);
        NavigationManager.getIns().startNavigation(ctx, loc, BaseNavigationProvider.NavigationMode.DRIVE, avoids);
        sNavigating = true;
    }

    /**
     * This is a workaround ...
     */
    public synchronized static void stopNavigation(final Context ctx) {
        if (!sNavigating) {
            if (LogUtil.DEBUG) LogUtil.log("NaviSceneUtil", "Not navigating, ignore command");
            return;
        }

        if (LogUtil.DEBUG) {
            LogUtil.log("NaviSceneUtil", "Start to stop navigation ...");
        }

        sNavigating = false;
        NavigationManager.getIns().stopNavigation(ctx);
        AsyncThread.getIns().executeDelay(new Runnable() {
            @Override
            public void run() {
                IntentUtil.openKikaGo(ctx);
            }
        }, 3000);
    }
}