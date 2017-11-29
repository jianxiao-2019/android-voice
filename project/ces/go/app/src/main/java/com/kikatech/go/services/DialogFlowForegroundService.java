package com.kikatech.go.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.R;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.ui.dialog.KikaStopServiceDialogActivity;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.usb.util.ImageUtil;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class DialogFlowForegroundService extends BaseForegroundService {
    private static final String TAG = "DialogFlowForegroundService";

    private final class BroadcastInfos {
        private static final String ACTION_ON_STATUS_CHANGED = "action_status_changed";
        private static final String ACTION_ON_NAVIGATION_STARTED = "action_navigation_started";
        private static final String ACTION_ON_NAVIGATION_STOPPED = "action_navigation_stopped";

        private static final String PARAM_STATUS = "param_status";
    }

    private static WindowManager mWindowManager;
    private static LayoutInflater mLayoutInflater;
    private static WindowManager.LayoutParams mLayoutParam = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
    );
    private View mView;
    private ImageView mStatusView;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleReceive(context, intent);
        }

        private void handleReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case BroadcastInfos.ACTION_ON_STATUS_CHANGED:
                    if (isViewAdded()) {
                        handleStatusChanged(intent);
                    }
                    break;
                case BroadcastInfos.ACTION_ON_NAVIGATION_STARTED:
                    showGMap();
                    break;
                case BroadcastInfos.ACTION_ON_NAVIGATION_STOPPED:
                    removeView();
                    break;
            }
        }
    };


    @Override
    protected void onStartForeground() {
        registerReceiver();
    }

    @Override
    protected void onStopForeground() {
        unregisterReceiver();
        removeView();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onStopForegroundWithConfirm() {
        Intent showDialogIntent = new Intent(this, KikaStopServiceDialogActivity.class);
        showDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        IntentUtil.sendPendingIntent(DialogFlowForegroundService.this, showDialogIntent);
    }


    private void showGMap() {
        if (isViewAdded()) {
            return;
        }

        mView = mLayoutInflater.inflate(R.layout.go_layout_gmap, null);

        mStatusView = (ImageView) mView.findViewById(R.id.gmap_status);

        addView();
    }

    private void addView() {
        try {
            // mLayoutParam.softInputMode = LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_ADJUST_PAN;
            mLayoutParam.gravity = Gravity.TOP | Gravity.RIGHT;
            mLayoutParam.x = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 14);
            mLayoutParam.y = ResolutionUtil.dp2px(DialogFlowForegroundService.this, 14);
            mWindowManager.addView(mView, mLayoutParam);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    private void removeView() {
        try {
            mWindowManager.removeView(mView);
        } catch (Exception ignore) {
        }
    }

    private boolean isViewAdded() {
        try {
            return mView != null && mView.getWindowToken() != null;
        } catch (Exception ignore) {
        }
        return true;
    }


    private void registerReceiver() {
        LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).registerReceiver(mReceiver, new IntentFilter(BroadcastInfos.ACTION_ON_STATUS_CHANGED));
        LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).registerReceiver(mReceiver, new IntentFilter(BroadcastInfos.ACTION_ON_NAVIGATION_STARTED));
        LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).registerReceiver(mReceiver, new IntentFilter(BroadcastInfos.ACTION_ON_NAVIGATION_STOPPED));
    }

    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(DialogFlowForegroundService.this).unregisterReceiver(mReceiver);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        onStopForeground();
        super.onDestroy();
    }

    private void handleStatusChanged(Intent intent) {
        GoLayout.ViewStatus status = (GoLayout.ViewStatus) intent.getSerializableExtra(BroadcastInfos.PARAM_STATUS);
        Glide.with(DialogFlowForegroundService.this)
                .load(status.getRes())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mStatusView);
    }


    public synchronized static void processStatusChanged(Context context, GoLayout.ViewStatus status) {
        Intent intent = new Intent(BroadcastInfos.ACTION_ON_STATUS_CHANGED);
        intent.putExtra(BroadcastInfos.PARAM_STATUS, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public synchronized static void processNavigationStarted(Context context) {
        Intent intent = new Intent(BroadcastInfos.ACTION_ON_NAVIGATION_STARTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    public synchronized static void processNavigationStopped(Context context) {
        Intent intent = new Intent(BroadcastInfos.ACTION_ON_NAVIGATION_STOPPED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    @Override
    protected Notification getForegroundNotification() {
        Intent closeIntent = new Intent(DialogFlowForegroundService.this, DialogFlowForegroundService.class);
        closeIntent.setAction(Commands.STOP_FOREGROUND_WITH_CONFIRM);
        PendingIntent closePendingIntent = PendingIntent.getService(DialogFlowForegroundService.this, getServiceId(), closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(DialogFlowForegroundService.this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Kika Go 录音中")
                .setContentText("点击此处结束Kika Go")
                .setContentIntent(closePendingIntent)
                .setAutoCancel(true)
                // .setColor( appCtx.getResources().getColor( R.color.gela_green ) )
                .build();
    }

    @Override
    protected int getServiceId() {
        return ServiceIds.DIALOG_FLOW_SERVICE;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
