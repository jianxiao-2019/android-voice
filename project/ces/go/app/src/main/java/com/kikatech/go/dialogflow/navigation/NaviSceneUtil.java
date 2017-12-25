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
import com.kikatech.go.util.StringUtil;

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
                destination = StringUtil.upperCaseFirstWord(destination);
            } catch (Exception ignore) {
            }
        }
        return destination;
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

        if (LogUtil.DEBUG) {
            LogUtil.log("NaviSceneUtil", "Start to stop navigation ..., targetClz:" + targetClz);
        }

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