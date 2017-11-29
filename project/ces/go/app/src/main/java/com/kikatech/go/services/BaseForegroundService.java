package com.kikatech.go.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public abstract class BaseForegroundService extends Service {
    private static final String TAG = "BaseForegroundService";

    protected abstract void onStartForeground();

    protected abstract void onStopForeground();

    protected abstract void onStopForegroundWithConfirm();

    protected abstract int getServiceId();

    protected abstract Notification getForegroundNotification();


    protected final class ServiceIds {
        private static final int SERVICE_ID = 0;
        public static final int DIALOG_FLOW_SERVICE = SERVICE_ID + 1;
    }

    protected static class Commands {
        private static final String FOREGROUND_SERVICE = "fore_ground_service_";
        public static final String START_FOREGROUND = FOREGROUND_SERVICE + "start_foreground";
        public static final String STOP_FOREGROUND = FOREGROUND_SERVICE + "stop_foreground";
        public static final String STOP_FOREGROUND_WITH_CONFIRM = FOREGROUND_SERVICE + "stop_foreground_with_confirm";
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //noinspection ConstantConditions
            switch (intent.getAction()) {
                case Commands.START_FOREGROUND:
                    handleStart();
                    break;
                case Commands.STOP_FOREGROUND:
                    handleStop();
                    break;
                case Commands.STOP_FOREGROUND_WITH_CONFIRM:
                    handleStopWithConfirm();
                    break;
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
        }

        return START_STICKY;
    }


    private void handleStart() {
        startForeground(getServiceId(), getForegroundNotification());
        onStartForeground();
    }

    private void handleStop() {
        stopForeground(true);
        onStopForeground();
    }

    private void handleStopWithConfirm() {
        onStopForegroundWithConfirm();
    }


    public static synchronized void processStart(Context context, Class<?> cls) {
        Bundle args = new Bundle();
        launchCommend(context, Commands.START_FOREGROUND, args, cls);
    }

    public static synchronized void processStop(Context context, Class<?> cls) {
        Bundle args = new Bundle();
        launchCommend(context, Commands.STOP_FOREGROUND, args, cls);
    }

    protected static synchronized void launchCommend(Context context, String action, Bundle args, Class<?> cls) {
        try {
            Context appCtx = context.getApplicationContext();
            Intent commendIntent = new Intent(appCtx, cls);
            commendIntent.setAction(action);
            commendIntent.putExtras(args);
            appCtx.startService(commendIntent);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }
}
