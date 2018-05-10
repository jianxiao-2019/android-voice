package com.kikatech.go.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.DFServiceStatus;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.eventbus.MusicEvent;
import com.kikatech.go.eventbus.ToDFServiceEvent;
import com.kikatech.go.services.presenter.DialogFlowServicePresenter;
import com.kikatech.go.services.view.manager.FloatingUiManager;
import com.kikatech.go.util.ImageUtil;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogOnViewUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.NetworkUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.usb.UsbAudioSource;
import com.xiao.usbaudio.AudioPlayBack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class DialogFlowForegroundService extends BaseForegroundService {
    private static final String TAG = "DialogFlowForegroundService";

    public static final String VOICE_SOURCE_ANDROID = "Android";
    public static final String VOICE_SOURCE_USB = "USB";

    private static class Commands extends BaseForegroundService.Commands {
        private static final String DIALOG_FLOW_SERVICE = "dialog_flow_service_";
        private static final String OPEN_KIKA_GO = DIALOG_FLOW_SERVICE + "open_kika_go";
    }

    private FloatingUiManager mManager;
    private DFServiceStatus mDFServiceStatus = new DFServiceStatus();

    private PowerManager.WakeLock mWakeLocker;

    private DialogFlowServicePresenter mDFPresenter;

    private static boolean isDoingAccessibility = false;


    private static boolean isAppForeground = true;


    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event sent to {@link com.kikatech.go.services.DialogFlowForegroundService}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onToServiceEvent(ToDFServiceEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        DFServiceEvent serviceEvent;
        switch (action) {
            case ToDFServiceEvent.ACTION_CHANGE_SERVER:
                if (mDFPresenter != null) {
                    mDFPresenter.updateVoiceSource();
                }
                break;
            case ToDFServiceEvent.ACTION_PING_SERVICE_STATUS:
                serviceEvent = new DFServiceEvent(DFServiceEvent.ACTION_ON_PING_SERVICE_STATUS);
                serviceEvent.putExtra(DFServiceEvent.PARAM_SERVICE_STATUS, mDFServiceStatus);
                serviceEvent.send();
                break;
            case ToDFServiceEvent.ACTION_SCAN_USB_DEVICES:
                if (mDFPresenter != null) {
                    mDFPresenter.scanUsbDevices();
                }
                break;
            case ToDFServiceEvent.ACTION_ON_APP_FOREGROUND:
                mDFServiceStatus.setAppForeground(true);
                if (mDFPresenter != null) {
                    mDFPresenter.enableUsbDetection();
                }
                mManager.setShowGMap(false);
                mManager.updateGMapVisibility();
                break;
            case ToDFServiceEvent.ACTION_ON_APP_BACKGROUND:
                mDFServiceStatus.setAppForeground(false);
                if (mDFPresenter != null) {
                    mDFPresenter.disableUsbDetection();
                }
                mManager.setShowGMap(true);
                if (!isDoingAccessibility) {
                    mManager.updateGMapVisibility();
                }
                break;
            case ToDFServiceEvent.ACTION_ON_STATUS_CHANGED:
                GoLayout.ViewStatus status = (GoLayout.ViewStatus) event.getExtras().getSerializable(ToDFServiceEvent.PARAM_STATUS);
                mManager.handleStatusChanged(status);
                break;
            case ToDFServiceEvent.ACTION_ON_MSG_CHANGED:
                String msg = event.getExtras().getString(ToDFServiceEvent.PARAM_TEXT);
                mManager.handleMsgChanged(msg);
                break;
            case ToDFServiceEvent.ACTION_ON_NAVIGATION_STARTED:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                break;
            case ToDFServiceEvent.ACTION_ON_NAVIGATION_STOPPED:
                break;
            case ToDFServiceEvent.ACTION_DIALOG_FLOW_TALK:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                String text = event.getExtras().getString(ToDFServiceEvent.PARAM_TEXT);
                if (mDFPresenter != null) {
                    mDFPresenter.talk(text, true);
                }
                break;
            case ToDFServiceEvent.ACTION_DIALOG_FLOW_WAKE_UP:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                if (mDFPresenter != null) {
                    mDFPresenter.wakeUp("main_click");
                }
                break;
            case ToDFServiceEvent.ACTION_PING_VOICE_SOURCE:
                serviceEvent = new DFServiceEvent(DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE);
                UsbAudioSource source = mDFPresenter != null ? mDFPresenter.getUsbVoiceSource() : null;
                serviceEvent.putExtra(DFServiceEvent.PARAM_TEXT, source == null ? VOICE_SOURCE_ANDROID : VOICE_SOURCE_USB);
                serviceEvent.send();
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("updateVoiceSource, mUsbVoiceSource: %s", source));
                }
                break;
            case ToDFServiceEvent.ACTION_ACCESSIBILITY_STARTED:
                setDoingAccessibility(true);
                // hide the item temporarily
                mManager.hideAllItems();
                break;
            case ToDFServiceEvent.ACTION_ACCESSIBILITY_STOPPED:
                setDoingAccessibility(false);
                // resume to its original visibility
                mManager.updateGMapVisibility();
                break;
            case ToDFServiceEvent.ACTION_DISABLE_WAKE_UP_DETECTOR:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                if (mDFPresenter != null) {
                    mDFPresenter.disableWakeUpDetector();
                }
                break;
            case ToDFServiceEvent.ACTION_ENABLE_WAKE_UP_DETECTOR:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                if (mDFPresenter != null) {
                    mDFPresenter.enableWakeUpDetector();
                }
                break;
            case ToDFServiceEvent.ACTION_ON_NEW_MSG:
                final boolean isServiceAwake = mDFServiceStatus.isAwake();
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s, isServiceAwake: %s", action, isServiceAwake));
                }
                String msgCommend = event.getExtras().getString(ToDFServiceEvent.PARAM_MSG_COMMEND);
                long msgTimestamp = event.getExtras().getLong(ToDFServiceEvent.PARAM_TIMESTAMP);
                if (mDFPresenter != null) {
                    mDFPresenter.doOnReceiveNewMsg(msgCommend, msgTimestamp);
                }
                break;
        }
    }

    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicEvent(MusicEvent event) {
        if (event == null) {
            return;
        }
        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("action: %s", action));
        }
        switch (action) {
            case MusicEvent.ACTION_ON_START:
            case MusicEvent.ACTION_ON_RESUME:
                if (!mDFServiceStatus.isAwake()) {
                    if (mDFPresenter != null) {
                        mDFPresenter.usbVolumeDown();
                    }
                }
                break;
            case MusicEvent.ACTION_ON_PAUSE:
            case MusicEvent.ACTION_ON_STOP:
                if (mDFPresenter != null) {
                    mDFPresenter.usbVolumeUp();
                }
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            performOnReceive(context, intent);
        }

        @SuppressWarnings("unused")
        private void performOnReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case Intent.ACTION_SCREEN_OFF:
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "onScreenOff");
                    }
                    if (mDFPresenter != null) {
                        mDFPresenter.doOnScreenLock();
                    }
                    break;
                case Intent.ACTION_USER_PRESENT:
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "onScreenUnlock");
                    }
                    if (mDFPresenter != null) {
                        mDFPresenter.doOnScreenUnlock();
                    }
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "onConnectivityChanged");
                    }
                    if (NetworkUtil.isNetworkAvailable(DialogFlowForegroundService.this)) {
                        if (mDFPresenter != null) {
                            mDFPresenter.updateVoiceSource();
                        }
                    }
                    new DFServiceEvent(DFServiceEvent.ACTION_ON_CONNECTIVITY_CHANGED).send();
                    break;
            }
        }
    };


    @Override
    protected void onStartForeground() {
        LogOnViewUtil.getIns().configFilterClass("com.kikatech.go.dialogflow.");

        registerReceiver();

        mManager = new FloatingUiManager.Builder()
                .setWindowManager((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .setLayoutInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .setConfiguration(getResources().getConfiguration())
                .setOnFloatingItemAction(new FloatingUiManager.IOnFloatingItemAction() {
                    @Override
                    public void onGMapClicked() {
                        if (mDFPresenter != null) {
                            mDFPresenter.wakeUp("floating");
                        }
                    }
                })
                .build(DialogFlowForegroundService.this);

        mDFPresenter = new DialogFlowServicePresenter(DialogFlowForegroundService.this, mDFServiceStatus, mManager);

        AudioPlayBack.setListener(new AudioPlayBack.OnAudioPlayBackWriteListener() {
            @Override
            public void onWrite(int len) {
                if (mDFServiceStatus.isUsbDeviceAvailable()) {
                    boolean isValidRawDataLen = len >= AudioPlayBack.RAW_DATA_AVAILABLE_LENGTH;
                    if (mDFServiceStatus.isUsbDeviceDataCorrect() == null || mDFServiceStatus.isUsbDeviceDataCorrect() != isValidRawDataLen) {
                        mDFServiceStatus.setUsbDeviceDataCorrect(isValidRawDataLen);
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED);
                        event.putExtra(DFServiceEvent.PARAM_IS_USB_DEVICE_DATA_CORRECT, isValidRawDataLen);
                        event.send();
                    }
                }
            }
        });

        acquireWakeLock();
    }

    @Override
    protected void onStopForeground() {
        Toast.makeText(DialogFlowForegroundService.this, "KikaGo is closed", Toast.LENGTH_SHORT).show();
        releaseWakeLock();
        unregisterReceiver();

        if (mManager != null) {
            mManager.removeGMap();
        }

        if (mDFPresenter != null) {
            mDFPresenter.quitService();
        }

        String action = DFServiceEvent.ACTION_EXIT_APP;
        DFServiceEvent event = new DFServiceEvent(action);
        event.send();

        if (LogOnViewUtil.ENABLE_LOG_FILE) {
            LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "Exit App, Goodbye !");
        }

        MusicForegroundService.stopMusic(this);

        AudioPlayBack.setListener(null);
    }

    @Override
    protected void onStopForegroundWithConfirm() {
    }


    @SuppressLint("WakelockTimeout")
    @SuppressWarnings("deprecation")
    private void acquireWakeLock() {
        if (mWakeLocker == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm == null) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "PowerManager is null, return");
                }
                return;
            }
            mWakeLocker = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
            mWakeLocker.setReferenceCounted(false);
        }
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "acquireWakeLock");
        }
        mWakeLocker.acquire();
    }

    private void releaseWakeLock() {
        if (mWakeLocker != null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "releaseWakeLock");
            }
            mWakeLocker.release();
            if (!mWakeLocker.isHeld()) {
                mWakeLocker = null;
            }
        }
    }


    private void registerReceiver() {
        unregisterReceiver();
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mReceiver, filter);
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(mReceiver);
            EventBus.getDefault().unregister(this);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "intent is null");
            }
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "action is empty");
            }
            return START_NOT_STICKY;
        }
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onStartCommand: " + intent.getAction());
        }
        //noinspection ConstantConditions
        switch (action) {
            case Commands.OPEN_KIKA_GO:
                IntentUtil.openKikaGo(DialogFlowForegroundService.this);
                return START_STICKY;
            default:
                return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public void onDestroy() {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "onDestroy");
        }
        onStopForeground();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mManager.updateConfiguration(newConfig);
    }


    public static boolean isAppForeground() {
        return isAppForeground;
    }

    private static void setDoingAccessibility(boolean doingAccessibility) {
        isDoingAccessibility = doingAccessibility;
    }


    public synchronized static void processPingDialogFlowStatus() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_PING_SERVICE_STATUS);
        event.send();
    }

    public synchronized static void processScanUsbDevices() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_SCAN_USB_DEVICES);
        event.send();
    }

    public synchronized static void processOnAppForeground() {
        isAppForeground = true;
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_APP_FOREGROUND);
        event.send();
    }

    public synchronized static void processOnAppBackground() {
        isAppForeground = false;
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_APP_BACKGROUND);
        event.send();
    }

    public synchronized static void processStatusChanged(GoLayout.ViewStatus status) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_STATUS_CHANGED);
        event.putExtra(ToDFServiceEvent.PARAM_STATUS, status);
        event.send();
    }

    public synchronized static void processMsgChanged(String text) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_MSG_CHANGED);
        event.putExtra(ToDFServiceEvent.PARAM_TEXT, text);
        event.send();
    }

    public synchronized static void processNavigationStarted() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_NAVIGATION_STARTED);
        event.send();
    }

    public synchronized static void processNavigationStopped() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_NAVIGATION_STOPPED);
        event.send();
    }

    public synchronized static void processDialogFlowTalk(String text) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_DIALOG_FLOW_TALK);
        event.putExtra(ToDFServiceEvent.PARAM_TEXT, text);
        event.send();
    }

    public synchronized static void processDialogFlowWakeUp() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_DIALOG_FLOW_WAKE_UP);
        event.send();
    }

    public synchronized static void processPingVoiceSource() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_PING_VOICE_SOURCE);
        event.send();
    }

    public synchronized static void processAccessibilityStarted() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ACCESSIBILITY_STARTED);
        event.send();
    }

    public synchronized static void processAccessibilityStopped() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ACCESSIBILITY_STOPPED);
        event.send();
    }

    public synchronized static void processDisableWakeUpDetector() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_DISABLE_WAKE_UP_DETECTOR);
        event.send();
    }

    public synchronized static void processEnableWakeUpDetector() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ENABLE_WAKE_UP_DETECTOR);
        event.send();
    }

    public synchronized static void processOnNewMsg(String commend, long msgTimestamp) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_NEW_MSG);
        event.putExtra(ToDFServiceEvent.PARAM_MSG_COMMEND, commend);
        event.putExtra(ToDFServiceEvent.PARAM_TIMESTAMP, msgTimestamp);
        event.send();
    }


    @Override
    protected Notification getForegroundNotification() {
        Intent openIntent = new Intent(DialogFlowForegroundService.this, DialogFlowForegroundService.class);
        openIntent.setAction(Commands.OPEN_KIKA_GO);
        PendingIntent openPendingIntent = PendingIntent.getService(DialogFlowForegroundService.this, getServiceId(), openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(DialogFlowForegroundService.this, DialogFlowForegroundService.class);
        closeIntent.setAction(Commands.STOP_FOREGROUND);
        PendingIntent closePendingIntent = PendingIntent.getService(DialogFlowForegroundService.this, getServiceId(), closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_dialogflow_foreground_service);// set your custom layout

        contentView.setOnClickPendingIntent(R.id.notification_btn_close, closePendingIntent);

        return new NotificationCompat.Builder(DialogFlowForegroundService.this)
                .setContent(contentView)
                .setCustomBigContentView(contentView)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.app_icon))
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
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