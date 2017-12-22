package com.kikatech.go.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.Toast;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.common.CommonSceneManager;
import com.kikatech.go.dialogflow.im.IMSceneManager;
import com.kikatech.go.dialogflow.navigation.NaviSceneManager;
import com.kikatech.go.dialogflow.sms.SmsSceneManager;
import com.kikatech.go.dialogflow.stop.SceneStopIntentManager;
import com.kikatech.go.dialogflow.telephony.TelephonySceneManager;
import com.kikatech.go.eventbus.DFServiceEvent;
import com.kikatech.go.eventbus.ToDFServiceEvent;
import com.kikatech.go.services.view.FloatingUiManager;
import com.kikatech.go.ui.KikaAlphaUiActivity;
import com.kikatech.go.ui.KikaMultiDexApplication;
import com.kikatech.go.ui.dialog.KikaStopServiceDialogActivity;
import com.kikatech.go.util.AsyncThread;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogOnViewUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.usb.util.ImageUtil;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.DialogFlowService;
import com.kikatech.voice.service.IDialogFlowService;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.conf.AsrConfiguration;

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

    private static final long TTS_DELAY_ASR_RESUME = 500;

    private static class Commands extends BaseForegroundService.Commands {
        private static final String DIALOG_FLOW_SERVICE = "dialog_flow_service_";
        private static final String OPEN_KIKA_GO = DIALOG_FLOW_SERVICE + "open_kika_go";
    }

    private FloatingUiManager mManager;

    private PowerManager.WakeLock mWakeLocker;

    private IDialogFlowService mDialogFlowService;
    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();

    private UsbAudioSource mAudioSource;
    private long start_t;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private final static int TIME_OUT_MS = 800;
    final Runnable mTimeOutTask = new Runnable() {
        @Override
        public void run() {
            onTimeout();
        }
    };

    private boolean asrActive;

    private boolean serviceActive = false;


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
        switch (action) {
            case ToDFServiceEvent.ACTION_ON_APP_FOREGROUND:
                mManager.hideAllItems();
                break;
            case ToDFServiceEvent.ACTION_ON_APP_BACKGROUND:
                mManager.showAllItems();
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
                pauseAsr();
                mManager.showGMap();
                break;
            case ToDFServiceEvent.ACTION_ON_NAVIGATION_STOPPED:
                mManager.removeGMap();
                break;
            case ToDFServiceEvent.ACTION_DIALOG_FLOW_TALK:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                String text = event.getExtras().getString(ToDFServiceEvent.PARAM_TEXT);
                pauseAsr();
                mDialogFlowService.talk(text);
                break;
            case ToDFServiceEvent.ACTION_DIALOG_FLOW_WAKE_UP:
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("action: %s", action));
                }
                mDialogFlowService.wakeUp();
                break;
            case ToDFServiceEvent.ACTION_PING_VOICE_SOURCE:
                DFServiceEvent serviceEvent = new DFServiceEvent(DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE);
                serviceEvent.putExtra(DFServiceEvent.PARAM_TEXT, mAudioSource == null ? VOICE_SOURCE_ANDROID : VOICE_SOURCE_USB);
                sendDFServiceEvent(serviceEvent);
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "updateVoiceSource, mAudioSource:" + mAudioSource);
                }
                break;
        }
    }


    @Override
    protected void onStartForeground() {
        LogOnViewUtil.getIns().configFilterClass("com.kikatech.go.dialogflow.");
        registerReceiver();
        initUsbVoice();
        acquireWakeLock();
    }

    private void setupDialogFlowService() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "setupDialogFlowService, serviceActive:" + serviceActive);
        }
        if (serviceActive) {
            mMainHandler.post(new Runnable() {
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
        } else {
            closeUsbAudio();
            Context ctx = KikaMultiDexApplication.getAppContext();
            android.content.Intent intent = new android.content.Intent(ctx, KikaAlphaUiActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            IntentUtil.sendPendingIntent(ctx, intent);
        }
    }

    private void updateVoiceSource() {
        if (mDialogFlowService != null) {
            VoiceConfiguration config = DialogFlowConfig.getVoiceConfig(this, mAudioSource);
            mDialogFlowService.updateRecorderSource(config);
        }
    }

    public void onTimeout() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onTimeout, spend:" + (System.currentTimeMillis() - start_t) + " ms");
        }
        setupDialogFlowService();
    }

    IUsbAudioListener mUsbCallback = new IUsbAudioListener() {

        @Override
        public void onDeviceAttached(UsbAudioSource audioSource) {
            BackgroundThread.getHandler().removeCallbacks(mTimeOutTask);
            mAudioSource = audioSource;
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onDeviceAttached, spend:" + (System.currentTimeMillis() - start_t) + " ms, serviceActive:" + serviceActive);
            }
            setupDialogFlowService();
        }

        @Override
        public void onDeviceDetached() {
            BackgroundThread.getHandler().removeCallbacks(mTimeOutTask);
            mAudioSource = null;
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onDeviceDetached, spend:" + (System.currentTimeMillis() - start_t) + " ms, serviceActive:" + serviceActive);
            }
            setupDialogFlowService();
        }
    };

    private void initUsbVoice() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "initUsbVoice, mAudioSource:" + mAudioSource);
        }

        if (mAudioSource == null) {
            start_t = System.currentTimeMillis();

            BackgroundThread.getHandler().postDelayed(mTimeOutTask, TIME_OUT_MS);

            UsbAudioService audioService = UsbAudioService.getInstance(this);
            audioService.setListener(mUsbCallback);

            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "scanDevices ...");
            }
            audioService.scanDevices();
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "mAudioSource:" + mAudioSource);
            }
        }
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

        closeUsbAudio();
    }

    private void closeUsbAudio() {
        if (mAudioSource != null) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "closeUsbAudio");
            }
            mAudioSource.close();
            mAudioSource = null;
        }
    }

    @Override
    protected void onStopForegroundWithConfirm() {
        Intent showDialogIntent = new Intent(this, KikaStopServiceDialogActivity.class);
        showDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        IntentUtil.sendPendingIntent(DialogFlowForegroundService.this, showDialogIntent);
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

    private void initDialogFlowService() {
        VoiceConfiguration config = DialogFlowConfig.getVoiceConfig(this, mAudioSource);

        mDialogFlowService = DialogFlowService.queryService(this,
                config,
                new IDialogFlowService.IServiceCallback() {
                    @Override
                    public void onInitComplete() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onInitComplete");
                        }
                        String action = DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "init UI Done");
                        }
                    }

                    @Override
                    public void onWakeUp() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onWakeUp");
                        }
                        String action = DFServiceEvent.ACTION_ON_WAKE_UP;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        resumeAsr();
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "Hi Kika Wake Up");
                            LogOnViewUtil.getIns().addLog(DebugLogType.ASR_LISTENING.logType);
                        }
                    }

                    @Override
                    public void onSleep() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onSleep");
                        }
                        String action = DFServiceEvent.ACTION_ON_SLEEP;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "Hi Kika Sleep");
                        }
                    }

                    @Override
                    public void onVadBos() {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onVadBos");
                        }
                        pauseAsr();
                        mDialogFlowService.talkUncaught();
                    }

                    @Override
                    public void onASRPause() {
                        String action = DFServiceEvent.ACTION_ON_ASR_PAUSE;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            long spend = System.currentTimeMillis() - mDbgLogResumeStartTime;
                            int per = (int) (100 * ((float) mDbgLogASRRecogFullTime / spend));
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "asr section over (" + spend + " ms, " + per + "%)");
                        }
                    }

                    @Override
                    public void onASRResume() {
                        String action = DFServiceEvent.ACTION_ON_ASR_RESUME;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
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
                        if (!asrActive) {
                            return;
                        } else if (isFinished) {
                            pauseAsr();
                        }

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
                                builder.append(pair.first);
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
                        pauseAsr();
                        if(supportAsrInterrupted) {
                            resumeAsr();
                        }
                    }

                    @Override
                    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("isInterrupted: %1$s, delayAsrResume: %2$s", isInterrupted, delayAsrResume));
                        }
                        if (delayAsrResume) {
                            AsyncThread.getIns().executeDelay(new Runnable() {
                                @Override
                                public void run() {
                                    resumeAsr();
                                }
                            }, TTS_DELAY_ASR_RESUME);
                        } else {
                            resumeAsr();
                        }
                        String action = DFServiceEvent.ACTION_ON_STAGE_ACTION_DONE;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_IS_INTERRUPTED, isInterrupted);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "isInterrupted:" + isInterrupted);
                        }
                    }

                    @Override
                    public void onStageEvent(Bundle extras) {
                        String action = DFServiceEvent.ACTION_ON_STAGE_EVENT;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_EXTRAS, extras);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(getDbgAction(action), "Parameters:" + extras);
                        }
                    }

                    @Override
                    public void onSceneExit(boolean proactive) {
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "onSceneExit");
                        }
                        if (proactive) {
                            resumeAsr();
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
                        String voiceSource = mAudioSource == null ? VOICE_SOURCE_ANDROID : VOICE_SOURCE_USB;
                        String action = DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE;
                        DFServiceEvent event = new DFServiceEvent(action);
                        event.putExtra(DFServiceEvent.PARAM_TEXT, voiceSource);
                        sendDFServiceEvent(event);
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, "updateVoiceSource, mAudioSource:" + mAudioSource);
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
                    public void onStart() {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onStart");
                        pauseAsr();
                        String action = DFServiceEvent.ACTION_ON_AGENT_QUERY_START;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            mDbgLogAPIQueryUITime = System.currentTimeMillis();
                            LogOnViewUtil.getIns().addSeparator();
                            LogOnViewUtil.getIns().addLog(getDbgAction(action));
                        }
                    }

                    @Override
                    public void onComplete(String[] dbgMsg) {
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onComplete");
                        // dbgMsg[0] : scene - action
                        // dbgMsg[1] : parameters
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
                        if (LogUtil.DEBUG) LogUtil.log(TAG, "IAgentQueryStatus::onError" + e);
                        String action = DFServiceEvent.ACTION_ON_AGENT_QUERY_ERROR;
                        DFServiceEvent event = new DFServiceEvent(action);
                        sendDFServiceEvent(event);
                        if (LogOnViewUtil.ENABLE_LOG_FILE) {
                            LogOnViewUtil.getIns().addLog(DebugLogType.API_AI_ERROR.logType);
                            LogOnViewUtil.getIns().addSeparator();
                            LogOnViewUtil.getIns().addLog(getDbgAction(action));
                        }
                    }
                });

        // Register all scenes from scene mangers
        mSceneManagers.add(new TelephonySceneManager(this, mDialogFlowService));
        mSceneManagers.add(new NaviSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new SceneStopIntentManager(this, mDialogFlowService, KikaAlphaUiActivity.class));
        mSceneManagers.add(new SmsSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new IMSceneManager(this, mDialogFlowService));
        mSceneManagers.add(new CommonSceneManager(this, mDialogFlowService));
    }

    private void sendDFServiceEvent(DFServiceEvent event) {
        EventBus.getDefault().post(event);
    }

    private synchronized void pauseAsr() {
        if (asrActive) {
            asrActive = false;
            mDialogFlowService.pauseAsr();
        }
    }

    private synchronized void resumeAsr() {
        if (!asrActive) {
            mDialogFlowService.resumeAsr();
            asrActive = true;
        }
    }

    private void registerReceiver() {
        unregisterReceiver();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ignore) {
        }
    }

    private void unregisterReceiver() {
        try {
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
                            mDialogFlowService.wakeUp();
                        }
                    }
                })
                .build(DialogFlowForegroundService.this);

        serviceActive = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
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
        onStopForeground();

        super.onDestroy();

        serviceActive = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mManager.updateConfiguration(newConfig);
    }

    public synchronized static void processOnAppForeground() {
        ToDFServiceEvent event = new ToDFServiceEvent(ToDFServiceEvent.ACTION_ON_APP_FOREGROUND);
        sendToDFServiceEvent(event);
    }

    public synchronized static void processOnAppBackground() {
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

    private synchronized static void sendToDFServiceEvent(ToDFServiceEvent event) {
        EventBus.getDefault().post(event);
    }


    @Override
    protected Notification getForegroundNotification() {
        Intent openIntent = new Intent(DialogFlowForegroundService.this, DialogFlowForegroundService.class);
        openIntent.setAction(Commands.OPEN_KIKA_GO);
        PendingIntent openPendingIntent = PendingIntent.getService(DialogFlowForegroundService.this, getServiceId(), openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(DialogFlowForegroundService.this)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.app_icon))
                .setContentTitle("KikaGo is running in the background")
                .setContentText("Tap to open KikaGo")
                .setContentIntent(openPendingIntent)
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


    private String getDbgAction(String action) {
        return "[" + action.replace("action_on_", "") + "]";
    }


    private enum DebugLogType {
        ASR_LISTENING("ASR listening"),
        ASR_STOP("ASR result"),
        API_AI_START("Api.ai start query"),
        API_AI_STOP("Api.ai stop query"),
        API_AI_ERROR("Api.ai query error");

        private String logType;

        DebugLogType(String log) {
            this.logType = log;
        }
    }
}