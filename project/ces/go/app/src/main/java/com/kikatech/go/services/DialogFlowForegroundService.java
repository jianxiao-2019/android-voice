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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.common.CommonSceneManager;
import com.kikatech.go.dialogflow.error.ErrorSceneActions;
import com.kikatech.go.dialogflow.error.ErrorSceneManager;
import com.kikatech.go.dialogflow.error.SceneError;
import com.kikatech.go.dialogflow.gotomain.GotoMainSceneManager;
import com.kikatech.go.dialogflow.im.IMSceneManager;
import com.kikatech.go.dialogflow.im.reply.SceneReplyIM;
import com.kikatech.go.dialogflow.model.DFServiceStatus;
import com.kikatech.go.dialogflow.music.MusicSceneManager;
import com.kikatech.go.dialogflow.navigation.NaviSceneManager;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.dialogflow.sms.SmsSceneManager;
import com.kikatech.go.dialogflow.sms.reply.SceneReplySms;
import com.kikatech.go.dialogflow.stop.SceneStopIntentManager;
import com.kikatech.go.dialogflow.telephony.TelephonySceneManager;
import com.kikatech.go.dialogflow.telephony.incoming.SceneIncoming;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.eventbus.MusicEvent;
import com.kikatech.go.eventbus.ToDFServiceEvent;
import com.kikatech.go.navigation.NavigationManager;
import com.kikatech.go.services.view.manager.FloatingUiManager;
import com.kikatech.go.ui.activity.KikaGoActivity;
import com.kikatech.go.util.AsyncThreadPool;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogOnViewUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.MediaPlayerUtil;
import com.kikatech.go.util.NetworkUtil;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.util.timer.CountingTimer;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.util.ImageUtil;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.dialogflow.DialogFlowService;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;
import com.kikatech.voice.service.voice.VoiceService;
import com.xiao.usbaudio.AudioPlayBack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/28.
 */

public class DialogFlowForegroundService extends BaseForegroundService {
    private static final String TAG = "DialogFlowForegroundService";

    public static final String VOICE_SOURCE_ANDROID = "Android";
    public static final String VOICE_SOURCE_USB = "USB";

    private static final long TTS_DELAY_ASR_RESUME = 250;

    private static class Commands extends BaseForegroundService.Commands {
        private static final String DIALOG_FLOW_SERVICE = "dialog_flow_service_";
        private static final String OPEN_KIKA_GO = DIALOG_FLOW_SERVICE + "open_kika_go";
    }

    private FloatingUiManager mManager;

    private PowerManager.WakeLock mWakeLocker;

    private IDialogFlowService mDialogFlowService;
    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();

    private VoiceSourceHelper mVoiceSourceHelper = new VoiceSourceHelper();

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private DFServiceStatus mDFServiceStatus = new DFServiceStatus();

    private static boolean isAppForeground = true;

    private static boolean isDoingAccessibility = false;

    private boolean mDbgLogFirstAsrResult = false;
    private boolean mIsAsrFinished = false;
    private long mDbgLogAPIQueryUITime = 0;
    private long mDbgLogASRRecogStartTime = 0;
    private long mDbgLogResumeStartTime = 0;
    private long mDbgLogASRRecogFullTime = 0;


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
                updateVoiceSource();
                break;
            case ToDFServiceEvent.ACTION_PING_SERVICE_STATUS:
                serviceEvent = new DFServiceEvent(DFServiceEvent.ACTION_ON_PING_SERVICE_STATUS);
                serviceEvent.putExtra(DFServiceEvent.PARAM_SERVICE_STATUS, mDFServiceStatus);
                sendDFServiceEvent(serviceEvent);
                break;
            case ToDFServiceEvent.ACTION_SCAN_USB_DEVICES:
                mVoiceSourceHelper.scanUsbDevices(this);
                break;
            case ToDFServiceEvent.ACTION_ON_APP_FOREGROUND:
                mVoiceSourceHelper.enableUsbDetection(this);
                mManager.setShowGMap(false);
                mManager.updateGMapVisibility();
                break;
            case ToDFServiceEvent.ACTION_ON_APP_BACKGROUND:
                mVoiceSourceHelper.disableUsbDetection(this);
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
                cancelAsr();
                mDialogFlowService.cancelAsrAlignment();
                mDialogFlowService.talk(text, true);
                break;
            case ToDFServiceEvent.ACTION_DIALOG_FLOW_WAKE_UP:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                mDialogFlowService.wakeUp("main_click");
                break;
            case ToDFServiceEvent.ACTION_PING_VOICE_SOURCE:
                serviceEvent = new DFServiceEvent(DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE);
                serviceEvent.putExtra(DFServiceEvent.PARAM_TEXT, mVoiceSourceHelper.getUsbVoiceSource() == null ? VOICE_SOURCE_ANDROID : VOICE_SOURCE_USB);
                sendDFServiceEvent(serviceEvent);
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("updateVoiceSource, mUsbVoiceSource: %s", mVoiceSourceHelper.getUsbVoiceSource()));
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
                if (mDialogFlowService != null) {
                    mDialogFlowService.disableWakeUpDetector();
                }
                break;
            case ToDFServiceEvent.ACTION_ENABLE_WAKE_UP_DETECTOR:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                if (mDialogFlowService != null) {
                    mDialogFlowService.enableWakeUpDetector();
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
                    if (mVoiceSourceHelper != null) {
                        mVoiceSourceHelper.usbVolumeDown();
                    }
                }
                break;
            case MusicEvent.ACTION_ON_PAUSE:
            case MusicEvent.ACTION_ON_STOP:
                if (mVoiceSourceHelper != null) {
                    mVoiceSourceHelper.usbVolumeUp();
                }
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            performOnReceive(context, intent);
        }

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
                    if (mDialogFlowService != null && mDFServiceStatus.isInit()) {
                        if (LogUtil.DEBUG) {
                            LogUtil.logv(TAG, "disable WakeUp Detector");
                        }
                        mDialogFlowService.sleep();
                        mDialogFlowService.disableWakeUpDetector();
                    }
                    break;
                case Intent.ACTION_USER_PRESENT:
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "onScreenUnlock");
                    }
                    if (mDialogFlowService != null && mDFServiceStatus.isInit()) {
                        if (LogUtil.DEBUG) {
                            LogUtil.logv(TAG, "enable WakeUp Detector");
                        }
                        mDialogFlowService.enableWakeUpDetector();
                    }
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "onConnectivityChanged");
                    }
                    if (NetworkUtil.isNetworkAvailable(DialogFlowForegroundService.this)) {
                        updateVoiceSource();
                    }
                    DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_CONNECTIVITY_CHANGED);
                    sendDFServiceEvent(event);
                    break;
            }
        }
    };


    @Override
    protected void onStartForeground() {
        LogOnViewUtil.getIns().configFilterClass("com.kikatech.go.dialogflow.");
        registerReceiver();
        initVoiceSource();
        BackgroundThread.getHandler().post(new Runnable() {
            @Override
            public void run() {
                initDialogFlowService();
            }
        });
        acquireWakeLock();
    }

    @Override
    protected void onStopForeground() {
        Toast.makeText(DialogFlowForegroundService.this, "KikaGo is closed", Toast.LENGTH_SHORT).show();
        releaseWakeLock();
        unregisterReceiver();
        mManager.removeGMap();
        for (BaseSceneManager bcm : mSceneManagers) {
            if (bcm != null) bcm.close();
        }
        if (mDialogFlowService != null) {
            mDialogFlowService.quitService();
        }

        String action = DFServiceEvent.ACTION_EXIT_APP;
        DFServiceEvent event = new DFServiceEvent(action);
        sendDFServiceEvent(event);

        if (LogOnViewUtil.ENABLE_LOG_FILE) {
            LogOnViewUtil.getIns().addLog(getDbgAction(action), "Exit App, Goodbye !");
        }

        MusicForegroundService.stopMusic(this);

        mVoiceSourceHelper.closeDevice(this);

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

    // TODO: implement a dialogFlowService presenter for physical isolation?
    private void initDialogFlowService() {
        VoiceConfiguration config = DialogFlowConfig.getVoiceConfig(this, mVoiceSourceHelper.getUsbVoiceSource());

        mDFServiceStatus.setUsbDeviceAvailable(mVoiceSourceHelper.getUsbVoiceSource() != null);

        mDialogFlowService = DialogFlowService.queryService(this,
                config,
                new IDialogFlowService.IServiceCallback() {
                    @Override
                    public void onInitComplete() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onInitComplete");
                        }
                        mDFServiceStatus.setInit(true);
                        String action = DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "init UI Done");
                        }
                    }

                    @Override
                    public void onWakeUp(final String scene) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onWakeUp, scene:" + scene);
                        }
                        mDFServiceStatus.setAwake(true);
                        MusicForegroundService.pauseMusic();
                        String action = DFServiceEvent.ACTION_ON_WAKE_UP;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_WAKE_UP_FROM, scene);
                        sendDFServiceEvent(event);
                        switch (scene) {
                            case SceneReplyIM.SCENE:
                            case SceneReplySms.SCENE:
                            case SceneIncoming.SCENE:
                                startAsr();
                                break;
                            default:
                                MediaPlayerUtil.playAlert(DialogFlowForegroundService.this, R.raw.alert_dot, new MediaPlayerUtil.IPlayStatusListener() {
                                    @Override
                                    public void onStart() {
                                    }

                                    @Override
                                    public void onStop() {
                                        startAsr();
                                    }
                                });
                                break;
                        }
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "Hi Kika Wake Up");
                            LogOnViewUtil.getIns().addLog("ASR listening");
                        }
                    }

                    @Override
                    public void onSleep() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onSleep");
                        }
                        mDFServiceStatus.setAwake(false);
                        if (mDialogFlowService != null) {
                            mDialogFlowService.startListening(-1);
                        }
                        MusicForegroundService.resumeMusic();
                        String action = DFServiceEvent.ACTION_ON_SLEEP;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "Hi Kika Sleep");
                        }
                    }

                    @Override
                    public void onASRPause() {
                        String action = DFServiceEvent.ACTION_ON_ASR_PAUSE;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event); // TODO: redundant, remove later
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            long spend = System.currentTimeMillis() - mDbgLogResumeStartTime;
                            int per = (int) (100 * ((float) mDbgLogASRRecogFullTime / spend));
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "asr section over (" + spend + " ms, " + per + "%)");
                        }
                    }

                    @Override
                    public void onASRResume() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onASRResume");
                        }
                        String action = DFServiceEvent.ACTION_ON_ASR_RESUME;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event); // TODO: redundant, remove later
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addSeparator();
                            mDbgLogResumeStartTime = System.currentTimeMillis();
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "asr section start");
                        }
                    }

                    @Override
                    public void onASRResult(final String speechText, String emojiUnicode, boolean isFinished) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("speechText: %1$s, emoji: %2%s, isFinished: %3$s", speechText, emojiUnicode, isFinished));
                        }

                        if (isFinished) {
                            if (mAsrMaxDurationTimer.isCounting()) {
                                mAsrMaxDurationTimer.stop();
                            }
                        }

                        mManager.handleAsrResult(StringUtil.upperCaseFirstWord(speechText));

                        String action = DFServiceEvent.ACTION_ON_ASR_RESULT;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, speechText);
                        //event.putExtra(DFServiceEvent.PARAM_EMOJI, emojiUnicode);
                        event.putExtra(DFServiceEvent.PARAM_IS_FINISHED, isFinished);
                        sendDFServiceEvent(event);

                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            mIsAsrFinished = isFinished;
                            if (!mDbgLogFirstAsrResult) {
                                mDbgLogFirstAsrResult = true;
                                mDbgLogASRRecogStartTime = System.currentTimeMillis();
                            }
                            String finishMsg = isFinished ? "[OK]" : "";
                            mDbgLogASRRecogFullTime = System.currentTimeMillis() - mDbgLogASRRecogStartTime;
                            String spendTime = " (" + mDbgLogASRRecogFullTime + " ms)";
                            String concat = StringUtil.upperCaseFirstWord(speechText);
                            LogOnViewUtil.getIns().addLog(getDbgAction(action) + finishMsg, concat + spendTime);
                            if (mIsAsrFinished) {
                                mDbgLogFirstAsrResult = false;
                            }
                        }
                    }

                    @Override
                    public void onError(int reason) {
                        if (mAsrMaxDurationTimer.isCounting()) {
                            mAsrMaxDurationTimer.stop();
                        }
                        switch (reason) {
                            case VoiceService.ERR_RECORD_DATA_FAIL:
                                if (LogUtil.DEBUG) {
                                    LogUtil.logw(TAG, "ERR_RECORD_DATA_FAIL");
                                }
                                updateVoiceSource();
                                break;
                            case VoiceService.ERR_NO_SPEECH:
                                if (LogUtil.DEBUG) {
                                    LogUtil.logw(TAG, "ERR_NO_SPEECH");
                                }
                                mDialogFlowService.talkUncaught();
                                break;
                            case VoiceService.ERR_CONNECTION_ERROR:
                                if (LogUtil.DEBUG) {
                                    LogUtil.logw(TAG, "ERR_NO_SPEECH");
                                }
                                mDialogFlowService.onLocalIntent(SceneError.SCENE, ErrorSceneActions.ACTION_SERVER_CONNECTION_ERROR);
                                break;
                        }
                    }

                    @Override
                    public void onText(String text, Bundle extras) {
                        String action = DFServiceEvent.ACTION_ON_TEXT;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, text);
                        event.putExtra(DFServiceEvent.PARAM_EXTRAS, extras);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), text);
                        }
                    }

                    @Override
                    public void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras) {
                        StringBuilder builder = new StringBuilder();
                        if (pairs != null && pairs.length > 0) {
                            for (Pair<String, Integer> pair : pairs) {
                                if (pair != null) {
                                    builder.append(pair.first);
                                }
                            }
                        }
                        String text = builder.toString();
                        String action = DFServiceEvent.ACTION_ON_TEXT_PAIRS;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, text);
                        event.putExtra(DFServiceEvent.PARAM_EXTRAS, extras);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), text);
                        }
                    }

                    @Override
                    public void onStagePrepared(String scene, String action, SceneStage stage) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("scene: %1$s, action: %2$s, stage: %3$s", scene, action, stage.getClass().getSimpleName()));
                        }
                        String eventAction = DFServiceEvent.ACTION_ON_STAGE_PREPARED;
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_STAGE_PREPARED);
                        event.putExtra(DFServiceEvent.PARAM_SCENE, scene);
                        event.putExtra(DFServiceEvent.PARAM_SCENE_ACTION, action);
                        event.putExtra(DFServiceEvent.PARAM_SCENE_STAGE, stage);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(eventAction), stage.toString());
                        }
                    }

                    @Override
                    public void onStageActionStart(boolean supportAsrInterrupted) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onStageActionStart, supportAsrInterrupted:" + supportAsrInterrupted);
                        }
                        if (supportAsrInterrupted) {
                            startAsr(-1); // don't start bos timer
                        }
                    }

                    @Override
                    public void onStageActionDone(final boolean isInterrupted, final boolean delayAsrResume, final Integer overrideAsrBos) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("isInterrupted: %1$s, delayAsrResume: %2$s", isInterrupted, delayAsrResume));
                        }

                        String action = DFServiceEvent.ACTION_ON_STAGE_ACTION_DONE;
                        DFServiceEvent event = new DFServiceEvent(action);
                        if (overrideAsrBos != null) {
                            event.putExtra(DFServiceEvent.PARAM_BOS_DURATION, overrideAsrBos);
                        }
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "isInterrupted:" + isInterrupted);
                        }

                        if (!isInterrupted) {
                            MediaPlayerUtil.playAlert(DialogFlowForegroundService.this, R.raw.alert_dot, new MediaPlayerUtil.IPlayStatusListener() {
                                @Override
                                public void onStart() {
                                }

                                @Override
                                public void onStop() {
                                    doStartAsrOnStageActionDone(delayAsrResume, overrideAsrBos);
                                }
                            });
                        } else {
                            doStartAsrOnStageActionDone(delayAsrResume, overrideAsrBos);
                        }
                    }

                    private void doStartAsrOnStageActionDone(boolean delayAsrResume, Integer overrideAsrBos) {
                        if (delayAsrResume) {
                            if (overrideAsrBos != null) {
                                startAsrDelay(overrideAsrBos);
                            } else {
                                startAsrDelay();
                            }
                        } else {
                            if (overrideAsrBos != null) {
                                startAsr(overrideAsrBos);
                            } else {
                                startAsr();
                            }
                        }
                    }

                    @Override
                    public void onStageEvent(Bundle extras) {
                        String event = extras.getString(SceneUtil.EXTRA_EVENT, null);
                        if (event == null) {
                            return;
                        }
                        if (LogUtil.DEBUG) {
                            LogUtil.logd(TAG, String.format("event: %1$s, isNavigating: %2$s, isAppForeground: %3$s", event, NaviSceneUtil.isNavigating(), isAppForeground));
                        }
                        if (SceneUtil.EVENT_DISPLAY_MSG_SENT.equals(event) && !extras.getBoolean(SceneUtil.EXTRA_OPEN_KIKA_GO, isAppForeground)) {
                            boolean isSentSuccess = extras.getBoolean(SceneUtil.EXTRA_SEND_SUCCESS, true);
                            int alertRes = extras.getInt(SceneUtil.EXTRA_ALERT, 0);
                            mManager.handleMsgSentStatusChanged(isSentSuccess);
                            MediaPlayerUtil.playAlert(DialogFlowForegroundService.this, alertRes, null);
                            NavigationManager.getIns().showMap(DialogFlowForegroundService.this, false);
                        }
                        String action = DFServiceEvent.ACTION_ON_STAGE_EVENT;
                        DFServiceEvent serviceEvent = new DFServiceEvent(action);
                        serviceEvent.putExtra(DFServiceEvent.PARAM_EXTRAS, extras);
                        sendDFServiceEvent(serviceEvent);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "Parameters:" + extras);
                        }
                    }

                    @Override
                    public void onSceneExit(boolean proactive) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onSceneExit");
                        }

                        String action = DFServiceEvent.ACTION_ON_SCENE_EXIT;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_IS_PROACTIVE, proactive);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "proactive:" + proactive);
                        }
                    }

                    @Override
                    public void onAsrConfigChange(AsrConfiguration asrConfig) {
                        String asrConfigJson = asrConfig.toJsonString();
                        String action = DFServiceEvent.ACTION_ON_ASR_CONFIG;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, asrConfigJson);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addSeparator();
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), asrConfigJson);
                            LogOnViewUtil.getIns().addSeparator();
                        }
                    }

                    @Override
                    public void onRecorderSourceUpdate() {
                        String voiceSource = mVoiceSourceHelper.getUsbVoiceSource() == null ? VOICE_SOURCE_ANDROID : VOICE_SOURCE_USB;
                        String action = DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, voiceSource);
                        event.putExtra(DFServiceEvent.PARAM_IS_USB_DEVICE_DATA_CORRECT, mDFServiceStatus.isUsbDeviceDataCorrect());
                        sendDFServiceEvent(event);
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("updateVoiceSource, mUsbVoiceSource: %s", mVoiceSourceHelper.getUsbVoiceSource()));
                        }
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addSeparator();
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "Voice Source:" + voiceSource);
                            LogOnViewUtil.getIns().addSeparator();
                        }
                        LogOnViewUtil.getIns().updateVoiceSourceInfo(voiceSource);
                    }
                }, new IDialogFlowService.IAgentQueryStatus() {
                    @Override
                    public void onStart(boolean proactive) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "IAgentQueryStatus::onStart");
                        }
                        String action = DFServiceEvent.ACTION_ON_AGENT_QUERY_START;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_IS_PROACTIVE, proactive);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            mDbgLogAPIQueryUITime = System.currentTimeMillis();
                            LogOnViewUtil.getIns().addSeparator();
                            LogOnViewUtil.getIns().addLog(getDbgAction(action));
                        }
                    }

                    @Override
                    public void onComplete(String[] dbgMsg) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "IAgentQueryStatus::onComplete");
                        }
                        // dbgMsg[0] : scene - action
                        // dbgMsg[1] : parameters
                        if (dbgMsg == null) {
                            // TODO: this is work around for dbgMsg null situation, should confirm the reason
                            return;
                        }
                        String action = DFServiceEvent.ACTION_ON_AGENT_QUERY_COMPLETE;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_DBG_INTENT_ACTION, dbgMsg[0]);
                        event.putExtra(DFServiceEvent.PARAM_DBG_INTENT_PARMS, dbgMsg[1]);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            mDbgLogAPIQueryUITime = System.currentTimeMillis() - mDbgLogAPIQueryUITime;
                            LogOnViewUtil.getIns().addLog(getDbgAction(action) + " (" + mDbgLogAPIQueryUITime + "ms)", "\n" + dbgMsg[0] + "\n" + dbgMsg[1]);
                            LogOnViewUtil.getIns().addSeparator();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "IAgentQueryStatus::onError" + e);
                        }
                        mDialogFlowService.onLocalIntent(SceneError.SCENE, ErrorSceneActions.ACTION_DF_ENGINE_ERROR);
                        String action = DFServiceEvent.ACTION_ON_AGENT_QUERY_ERROR;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog("Api.ai query error");
                            LogOnViewUtil.getIns().addSeparator();
                            LogOnViewUtil.getIns().addLog(getDbgAction(action));
                        }
                    }
                });

        // Register all scenes from scene mangers
        mSceneManagers.add(new TelephonySceneManager(this, mDialogFlowService));
        mSceneManagers.add(new NaviSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new SceneStopIntentManager(this, mDialogFlowService, KikaGoActivity.class));
        mSceneManagers.add(new SmsSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new IMSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new CommonSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new GotoMainSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new MusicSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new ErrorSceneManager(this, mDialogFlowService));
    }

    private void setupDialogFlowService() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "setupDialogFlowService, mIsStarted:" + mIsStarted);
        }
        if (mIsStarted) {
            BackgroundThread.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    final long start_t = System.currentTimeMillis();
                    final String dbg;
                    if (mDialogFlowService == null) {
                        initDialogFlowService();
                        dbg = "initDialogFlowService";
                    } else {
                        updateVoiceSource();
                        dbg = "updateVoiceSource";
                    }
                    if (LogUtil.DEBUG) {
                        LogUtil.logv(TAG, dbg + " done, spend:" + (System.currentTimeMillis() - start_t) + " ms");
                    }
                }
            });
        }
    }

    private void initVoiceSource() {
        mVoiceSourceHelper.setVoiceSourceListener(new VoiceSourceHelper.IVoiceSourceListener() {
            @Override
            public void onVoiceSourceEvent(int event) {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("onVoiceSourceEvent, isStart: %s, event: %s, source: %s", mIsStarted, event, mVoiceSourceHelper.getUsbVoiceSource()));
                }
                if (!mIsStarted) {
                    return;
                }
                switch (event) {
                    case VoiceSourceHelper.Event.USB_ATTACHED:
                        setupDialogFlowService();
                        break;
                    case VoiceSourceHelper.Event.USB_DETACHED:
                        setupDialogFlowService();
                        break;
                    case VoiceSourceHelper.Event.USB_DEVICE_NOT_FOUND:
                        sendDFServiceEvent(new DFServiceEvent(DFServiceEvent.ACTION_ON_USB_NO_DEVICES));
                    case VoiceSourceHelper.Event.USB_DEVICE_ERROR:
                        mVoiceSourceHelper.closeDevice(DialogFlowForegroundService.this);
                        setupDialogFlowService();
                        break;
                    case VoiceSourceHelper.Event.NON_CHANGED:
                        DFServiceEvent serviceEvent = new DFServiceEvent(DFServiceEvent.ACTION_ON_USB_NON_CHANGED);
                        serviceEvent.putExtra(DFServiceEvent.PARAM_TEXT, mVoiceSourceHelper.getUsbVoiceSource() == null ? VOICE_SOURCE_ANDROID : VOICE_SOURCE_USB);
                        serviceEvent.putExtra(DFServiceEvent.PARAM_IS_USB_DEVICE_DATA_CORRECT, mDFServiceStatus.isUsbDeviceDataCorrect());
                        sendDFServiceEvent(serviceEvent);
                        break;
                }
            }
        });
    }

    private void updateVoiceSource() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("updateVoiceSource, source: %s", mVoiceSourceHelper.getUsbVoiceSource()));
        }
        if (mDialogFlowService != null) {
            if (mAsrMaxDurationTimer.isCounting()) {
                mAsrMaxDurationTimer.stop();
            }
            VoiceConfiguration config = DialogFlowConfig.getVoiceConfig(this, mVoiceSourceHelper.getUsbVoiceSource());
            mDFServiceStatus.setUsbDeviceAvailable(mVoiceSourceHelper.getUsbVoiceSource() != null);
            mDialogFlowService.updateRecorderSource(config);
        }
    }

    private void sendDFServiceEvent(DFServiceEvent event) {
        EventBus.getDefault().post(event);
    }


    private CountingTimer mAsrMaxDurationTimer = new CountingTimer(20000, new CountingTimer.ICountingListener() {
        @Override
        public void onTimeTickStart() {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onTimeTickStart");
            }
        }

        @Override
        public void onTimeTick(long millis) {
        }

        @Override
        public void onTimeTickEnd() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTimeTickEnd");
            }
            if (mDialogFlowService != null) {
                mDialogFlowService.completeListening();
            }
        }

        @Override
        public void onInterrupted(long stopMillis) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "onInterrupted");
            }
        }
    });

    private synchronized void startAsr() {
        if (mDialogFlowService != null) {
            mDialogFlowService.startListening();
            mAsrMaxDurationTimer.start();
        }
    }

    private synchronized void startAsr(int bosDuration) {
        if (mDialogFlowService != null) {
            mDialogFlowService.startListening(bosDuration);
            mAsrMaxDurationTimer.start();
        }
    }

    private synchronized void startAsrDelay() {
        AsyncThreadPool.getIns().executeDelay(new Runnable() {
            @Override
            public void run() {
                startAsr();
            }
        }, TTS_DELAY_ASR_RESUME);
    }

    private synchronized void startAsrDelay(final int bosDuration) {
        AsyncThreadPool.getIns().executeDelay(new Runnable() {
            @Override
            public void run() {
                startAsr(bosDuration);
            }
        }, TTS_DELAY_ASR_RESUME);
    }

    private synchronized void cancelAsr() {
        if (mDialogFlowService != null) {
            mDialogFlowService.cancelListening();
            if (mAsrMaxDurationTimer.isCounting()) {
                mAsrMaxDurationTimer.stop();
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
        mManager = new FloatingUiManager.Builder()
                .setWindowManager((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .setLayoutInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .setConfiguration(getResources().getConfiguration())
                .setOnFloatingItemAction(new FloatingUiManager.IOnFloatingItemAction() {
                    @Override
                    public void onGMapClicked() {
                        if (mDialogFlowService != null) {
                            mDialogFlowService.wakeUp("floating");
                        }
                    }
                })
                .build(DialogFlowForegroundService.this);
        AudioPlayBack.setListener(new AudioPlayBack.OnAudioPlayBackWriteListener() {
            @Override
            public void onWrite(int len) {
                if (mDFServiceStatus.isUsbDeviceAvailable()) {
                    boolean isValidRawDataLen = len >= AudioPlayBack.RAW_DATA_AVAILABLE_LENGTH;
                    if (mDFServiceStatus.isUsbDeviceDataCorrect() == null || mDFServiceStatus.isUsbDeviceDataCorrect() != isValidRawDataLen) {
                        mDFServiceStatus.setUsbDeviceDataCorrect(isValidRawDataLen);
                        DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED);
                        event.putExtra(DFServiceEvent.PARAM_IS_USB_DEVICE_DATA_CORRECT, isValidRawDataLen);
                        sendDFServiceEvent(event);
                    }
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onStartCommand: " + intent.getAction());
            }
            //noinspection ConstantConditions
            switch (intent.getAction()) {
                case Commands.OPEN_KIKA_GO:
                    IntentUtil.openKikaGo(DialogFlowForegroundService.this);
                    return START_STICKY;
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (LogUtil.DEBUG) {
            LogUtil.logw("SkTest", "onDestroy");
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
        sendToDFServiceEvent(event);
    }

    public synchronized static void processScanUsbDevices() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_SCAN_USB_DEVICES);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processOnAppForeground() {
        isAppForeground = true;
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_APP_FOREGROUND);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processOnAppBackground() {
        isAppForeground = false;
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_APP_BACKGROUND);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processStatusChanged(GoLayout.ViewStatus status) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_STATUS_CHANGED);
        event.putExtra(ToDFServiceEvent.PARAM_STATUS, status);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processMsgChanged(String text) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_MSG_CHANGED);
        event.putExtra(ToDFServiceEvent.PARAM_TEXT, text);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processNavigationStarted() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_NAVIGATION_STARTED);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processNavigationStopped() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_NAVIGATION_STOPPED);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processDialogFlowTalk(String text) {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_DIALOG_FLOW_TALK);
        event.putExtra(ToDFServiceEvent.PARAM_TEXT, text);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processDialogFlowWakeUp() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_DIALOG_FLOW_WAKE_UP);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processPingVoiceSource() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_PING_VOICE_SOURCE);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processAccessibilityStarted() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ACCESSIBILITY_STARTED);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processAccessibilityStopped() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ACCESSIBILITY_STOPPED);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processDisableWakeUpDetector() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_DISABLE_WAKE_UP_DETECTOR);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processEnableWakeUpDetector() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ENABLE_WAKE_UP_DETECTOR);
        sendToDFServiceEvent(event);
    }

    private synchronized static void sendToDFServiceEvent(ToDFServiceEvent event) {
        EventBus.getDefault().post(event);
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


    private String getDbgAction(String action) {
        return "[" + action.replace("action_on_", "") + "]";
    }
}