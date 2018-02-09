package com.kikatech.go.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.kikatech.go.eventbus.ToOobeServiceEvent;
import com.kikatech.go.services.view.manager.FloatingOobeManager;
import com.kikatech.go.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author SkeeterWang Created on 2018/2/9.
 */

public class OobeService extends Service {
    private static final String TAG = "OobeService";

    private static class Commands {
        private static final String OOBE_SERVICE = "oobe_service_";
        private static final String START = OOBE_SERVICE + "start";
        private static final String STOP = OOBE_SERVICE + "stop";
    }


    private FloatingOobeManager mManager;


    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event sent to {@link com.kikatech.go.services.DialogFlowForegroundService}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onToServiceEvent(ToOobeServiceEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case ToOobeServiceEvent.ACTION_SHOW_OOBE_UI:
                mManager.showOobeUi();
                break;
            case ToOobeServiceEvent.ACTION_HIDE_OOBE_UI:
                mManager.hideOobeUi();
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = new FloatingOobeManager.Builder()
                .setWindowManager((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .setLayoutInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .setConfiguration(getResources().getConfiguration())
                .build(OobeService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case Commands.START:
                        onServiceStart();
                        break;
                    case Commands.STOP:
                        stopSelf();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onServiceStop();
    }

    private synchronized void onServiceStart() {
        registerReceiver();
        mManager.addOobeUi();
    }

    private synchronized void onServiceStop() {
        unregisterReceiver();
        mManager.removeOobeUi();
    }

    private synchronized void registerReceiver() {
        unregisterReceiver();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private synchronized void unregisterReceiver() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ignore) {
        }
    }


    public static synchronized void startService(Context context) {
        launchCommend(context, Commands.START, new Bundle());
    }

    public static synchronized void stopService(Context context) {
        launchCommend(context, Commands.STOP, new Bundle());
    }

    private static synchronized void launchCommend(Context context, String action, Bundle args) {
        try {
            Intent commendIntent = new Intent(context, OobeService.class);
            commendIntent.setAction(action);
            commendIntent.putExtras(args);
            context.startService(commendIntent);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }


    public static synchronized void showOobeUi() {
        ToOobeServiceEvent event = new ToOobeServiceEvent(ToOobeServiceEvent.ACTION_SHOW_OOBE_UI);
        sendToDFServiceEvent(event);
    }

    public static synchronized void hideOobeUi() {
        ToOobeServiceEvent event = new ToOobeServiceEvent(ToOobeServiceEvent.ACTION_HIDE_OOBE_UI);
        sendToDFServiceEvent(event);
    }

    private synchronized static void sendToDFServiceEvent(ToOobeServiceEvent event) {
        EventBus.getDefault().post(event);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
