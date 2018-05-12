package com.kikatech.go.services.presenter;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.close.CloseSceneManager;
import com.kikatech.go.dialogflow.close.SceneClose;
import com.kikatech.go.dialogflow.common.CommonSceneManager;
import com.kikatech.go.dialogflow.common.SceneCommon;
import com.kikatech.go.dialogflow.error.ErrorSceneActions;
import com.kikatech.go.dialogflow.error.ErrorSceneManager;
import com.kikatech.go.dialogflow.error.SceneError;
import com.kikatech.go.dialogflow.gotomain.GotoMainSceneManager;
import com.kikatech.go.dialogflow.help.HelpSceneManager;
import com.kikatech.go.dialogflow.help.SceneHelp;
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
import com.kikatech.go.navigation.NavigationManager;
import com.kikatech.go.services.MusicForegroundService;
import com.kikatech.go.services.view.manager.FloatingUiManager;
import com.kikatech.go.ui.activity.KikaGoActivity;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogOnViewUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.MediaPlayerUtil;
import com.kikatech.go.util.StringUtil;
import com.kikatech.go.util.timer.CountingTimer;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.dialogflow.DialogFlowService;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;
import com.kikatech.voice.service.voice.VoiceService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/**
 * @author SkeeterWang Created on 2018/5/8.
 */

public class DialogFlowServicePresenter {
    private static final String TAG = "DialogFlowServicePresenter";


    private Context mContext;

    private IDialogFlowService mDialogFlowService;

    private FloatingUiManager mManager;

    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();
    private CloseSceneManager mCloseSceneManager;
    private HelpSceneManager mHelpSceneManager;

    private VoiceSourceHelper mVoiceSourceHelper = new VoiceSourceHelper();

    private DFServiceStatus mDFServiceStatus = new DFServiceStatus();

    private Queue<MsgTask> mMsgQueue = new LinkedList<>();


    private boolean mDbgLogFirstAsrResult = false;
    private long mDbgLogAPIQueryUITime = 0;
    private long mDbgLogASRRecogStartTime = 0;
    private long mDbgLogResumeStartTime = 0;
    private long mDbgLogASRRecogFullTime = 0;


    public DialogFlowServicePresenter(Context context, DFServiceStatus dfServiceStatus, FloatingUiManager manager) {
        mContext = context;
        mDFServiceStatus = dfServiceStatus;
        mManager = manager;
        initVoiceSource();
        initDialogFlowService();
    }


    private IDialogFlowService.IServiceCallback mServiceCallback = new IDialogFlowService.IServiceCallback() {
        @Override
        public void onInitComplete() {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onInitComplete");
            }
            mDFServiceStatus.setInit(true);
            String action = DFServiceEvent.ACTION_ON_DIALOG_FLOW_INIT;
            DFServiceEvent event = new DFServiceEvent(action);
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "init UI Done");
            }
        }

        @Override
        public void onWakeUp(String scene) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onWakeUp, scene:" + scene);
            }
            mDFServiceStatus.setAwake(true);
            MusicForegroundService.pauseMusic();
            String action = DFServiceEvent.ACTION_ON_WAKE_UP;
            DFServiceEvent event = new DFServiceEvent(action);
            event.putExtra(DFServiceEvent.PARAM_WAKE_UP_FROM, scene);
            event.send();
            switch (scene) {
                case SceneReplyIM.SCENE:
                case SceneReplySms.SCENE:
                case SceneIncoming.SCENE:
                    startAsr();
                    break;
                default:
                    MediaPlayerUtil.playAlert(mContext, R.raw.alert_dot, new MediaPlayerUtil.IPlayStatusListener() {
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
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "Hi Kika Wake Up");
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
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "Hi Kika Sleep");
            }
            if (LogUtil.DEBUG) {
                for (BaseSceneManager bcm : mSceneManagers) {
                    if (bcm != null) {
                        LogUtil.logv(TAG, String.format("[SceneManager] %s", bcm.getClass().getSimpleName()));
                    }
                }
            }
            if (!mSceneManagers.contains(mCloseSceneManager)) {
                mCloseSceneManager = new CloseSceneManager(mContext, mDialogFlowService);
                mSceneManagers.add(mCloseSceneManager);
            }
            if (!mSceneManagers.contains(mCloseSceneManager)) {
                mHelpSceneManager = new HelpSceneManager(mContext, mDialogFlowService);
                mSceneManagers.add(mHelpSceneManager);
            }
            if (!mMsgQueue.isEmpty()) {
                MsgTask msgTask = mMsgQueue.poll();
                String msgCommend = msgTask.commend;
                long msgTimestamp = msgTask.timestamp;
                doOnNewMsg(msgCommend, msgTimestamp);
            }
        }

        @Override
        public void onASRPause() {
            String action = DFServiceEvent.ACTION_ON_ASR_PAUSE;
            DFServiceEvent event = new DFServiceEvent(action);
            event.send(); // TODO: redundant, remove later
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                long spend = System.currentTimeMillis() - mDbgLogResumeStartTime;
                int per = (int) (100 * ((float) mDbgLogASRRecogFullTime / spend));
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "asr section over (" + spend + " ms, " + per + "%)");
            }
        }

        @Override
        public void onASRResume() {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onASRResume");
            }
            String action = DFServiceEvent.ACTION_ON_ASR_RESUME;
            DFServiceEvent event = new DFServiceEvent(action);
            event.send(); // TODO: redundant, remove later
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addSeparator();
                mDbgLogResumeStartTime = System.currentTimeMillis();
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "asr section start");
            }
        }

        @Override
        public void onASRResult(String speechText, String emojiUnicode, boolean isFinished) {
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
            event.send();

            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                if (!mDbgLogFirstAsrResult) {
                    mDbgLogFirstAsrResult = true;
                    mDbgLogASRRecogStartTime = System.currentTimeMillis();
                }
                String finishMsg = isFinished ? "[OK]" : "";
                mDbgLogASRRecogFullTime = System.currentTimeMillis() - mDbgLogASRRecogStartTime;
                String spendTime = " (" + mDbgLogASRRecogFullTime + " ms)";
                String concat = StringUtil.upperCaseFirstWord(speechText);
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action) + finishMsg, concat + spendTime);
                if (isFinished) {
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
                case VoiceService.ERR_RECORD_OPEN_FAIL:
                    if (LogUtil.DEBUG) {
                        LogUtil.logw(TAG, "ERR_RECORD_OPEN_FAIL");
                    }
                case VoiceService.ERR_RECORD_DATA_FAIL:
                    if (LogUtil.DEBUG) {
                        LogUtil.logw(TAG, "ERR_RECORD_DATA_FAIL");
                    }
                    mDFServiceStatus.setAudioDataCorrect(false);
                    String source = mVoiceSourceHelper.getUsbVoiceSource() != null ? VoiceSourceHelper.VOICE_SOURCE_USB : VoiceSourceHelper.VOICE_SOURCE_ANDROID;
                    DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED);
                    event.putExtra(DFServiceEvent.PARAM_AUDIO_SOURCE, source);
                    event.putExtra(DFServiceEvent.PARAM_IS_AUDIO_DATA_CORRECT, false);
                    event.send();
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
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), text);
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
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), text);
            }
        }

        @Override
        public void onStagePrepared(String scene, String action, SceneStage stage) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, String.format("scene: %1$s, action: %2$s, stage: %3$s", scene, action, stage.getClass().getSimpleName()));
            }
            if (!SceneError.SCENE.equals(scene) && !SceneCommon.SCENE.equals(scene)) {
                if (!SceneHelp.SCENE.equals(scene)) {
                    mHelpSceneManager.close();
                    mSceneManagers.remove(mHelpSceneManager);
                }
                if (!SceneClose.SCENE.equals(scene)) {
                    mCloseSceneManager.close();
                    mSceneManagers.remove(mCloseSceneManager);
                }
            }
            String eventAction = DFServiceEvent.ACTION_ON_STAGE_PREPARED;
            DFServiceEvent event = new DFServiceEvent(DFServiceEvent.ACTION_ON_STAGE_PREPARED);
            event.putExtra(DFServiceEvent.PARAM_SCENE, scene);
            event.putExtra(DFServiceEvent.PARAM_SCENE_ACTION, action);
            event.putExtra(DFServiceEvent.PARAM_SCENE_STAGE, stage);
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(eventAction), stage.toString());
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
        public void onStageActionDone(boolean isInterrupted, final Integer overrideAsrBos) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, String.format("isInterrupted: %s", isInterrupted));
            }

            String action = DFServiceEvent.ACTION_ON_STAGE_ACTION_DONE;
            DFServiceEvent event = new DFServiceEvent(action);
            if (overrideAsrBos != null) {
                event.putExtra(DFServiceEvent.PARAM_BOS_DURATION, overrideAsrBos);
            }
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "isInterrupted:" + isInterrupted);
            }

            if (!isInterrupted) {
                MediaPlayerUtil.playAlert(mContext, R.raw.alert_dot, new MediaPlayerUtil.IPlayStatusListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onStop() {
                        doStartAsrOnStageActionDone(overrideAsrBos);
                    }
                });
            } else {
                doStartAsrOnStageActionDone(overrideAsrBos);
            }
        }

        private void doStartAsrOnStageActionDone(Integer overrideAsrBos) {
            if (overrideAsrBos != null) {
                startAsr(overrideAsrBos);
            } else {
                startAsr();
            }
        }

        @Override
        public void onStageEvent(Bundle extras) {
            String event = extras.getString(SceneUtil.EXTRA_EVENT, null);
            if (event == null) {
                return;
            }
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, String.format("event: %1$s, isNavigating: %2$s, isAppForeground: %3$s", event, NaviSceneUtil.isNavigating(), mDFServiceStatus.isAppForeground()));
            }
            if (SceneUtil.EVENT_DISPLAY_MSG_SENT.equals(event) && !extras.getBoolean(SceneUtil.EXTRA_OPEN_KIKA_GO, mDFServiceStatus.isAppForeground())) {
                boolean isSentSuccess = extras.getBoolean(SceneUtil.EXTRA_SEND_SUCCESS, true);
                int alertRes = extras.getInt(SceneUtil.EXTRA_ALERT, 0);
                mManager.handleMsgSentStatusChanged(isSentSuccess);
                MediaPlayerUtil.playAlert(mContext, alertRes, null);
                NavigationManager.getIns().showMap(mContext, false);
            }
            String action = DFServiceEvent.ACTION_ON_STAGE_EVENT;
            DFServiceEvent serviceEvent = new DFServiceEvent(action);
            serviceEvent.putExtra(DFServiceEvent.PARAM_EXTRAS, extras);
            serviceEvent.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "Parameters:" + extras);
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
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "proactive:" + proactive);
            }
        }

        @Override
        public void onAsrConfigChange(AsrConfiguration asrConfig) {
            String asrConfigJson = asrConfig.toJsonString();
            String action = DFServiceEvent.ACTION_ON_ASR_CONFIG;
            DFServiceEvent event = new DFServiceEvent(action);
            event.putExtra(DFServiceEvent.PARAM_ASR_CONFIG_JSON, asrConfigJson);
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addSeparator();
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), asrConfigJson);
                LogOnViewUtil.getIns().addSeparator();
            }
        }

        @Override
        public void onRecorderSourceUpdate() {
            String voiceSource = mVoiceSourceHelper.getUsbVoiceSource() == null ? VoiceSourceHelper.VOICE_SOURCE_ANDROID : VoiceSourceHelper.VOICE_SOURCE_USB;
            String action = DFServiceEvent.ACTION_ON_VOICE_SRC_CHANGE;
            DFServiceEvent event = new DFServiceEvent(action);
            event.putExtra(DFServiceEvent.PARAM_AUDIO_SOURCE, voiceSource);
            event.putExtra(DFServiceEvent.PARAM_IS_AUDIO_DATA_CORRECT, mDFServiceStatus.isAudioDataCorrect());
            event.send();
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, String.format("updateVoiceSource, mUsbVoiceSource: %s", mVoiceSourceHelper.getUsbVoiceSource()));
            }
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addSeparator();
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action), "Voice Source:" + voiceSource);
                LogOnViewUtil.getIns().addSeparator();
            }
            LogOnViewUtil.getIns().updateVoiceSourceInfo(voiceSource);
        }
    };

    private IDialogFlowService.IAgentQueryStatus mAgentQueryStatus = new IDialogFlowService.IAgentQueryStatus() {
        @Override
        public void onStart(boolean proactive) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "IAgentQueryStatus::onStart");
            }
            String action = DFServiceEvent.ACTION_ON_AGENT_QUERY_START;
            DFServiceEvent event = new DFServiceEvent(action);
            event.putExtra(DFServiceEvent.PARAM_IS_PROACTIVE, proactive);
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                mDbgLogAPIQueryUITime = System.currentTimeMillis();
                LogOnViewUtil.getIns().addSeparator();
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action));
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
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                mDbgLogAPIQueryUITime = System.currentTimeMillis() - mDbgLogAPIQueryUITime;
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action) + " (" + mDbgLogAPIQueryUITime + "ms)", "\n" + dbgMsg[0] + "\n" + dbgMsg[1]);
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
            event.send();
            if (LogOnViewUtil.ENABLE_LOG_FILE) {
                LogOnViewUtil.getIns().addLog("Api.ai query error");
                LogOnViewUtil.getIns().addSeparator();
                LogOnViewUtil.getIns().addLog(LogOnViewUtil.getIns().getDbgActionLog(action));
            }
        }
    };


    private void initVoiceSource() {
        mVoiceSourceHelper.setVoiceSourceListener(new VoiceSourceHelper.IVoiceSourceListener() {
            @Override
            public void onVoiceSourceEvent(int event) {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("onVoiceSourceEvent, event: %s, source: %s", event, mVoiceSourceHelper.getUsbVoiceSource()));
                }
                switch (event) {
                    case VoiceSourceHelper.Event.USB_ATTACHED:
                        setupDialogFlowService();
                        break;
                    case VoiceSourceHelper.Event.USB_DETACHED:
                        setupDialogFlowService();
                        break;
                    case VoiceSourceHelper.Event.USB_DEVICE_NOT_FOUND:
                        new DFServiceEvent(DFServiceEvent.ACTION_ON_USB_NO_DEVICES).send();
                    case VoiceSourceHelper.Event.USB_DEVICE_ERROR:
                        mVoiceSourceHelper.closeDevice(mContext);
                        setupDialogFlowService();
                        break;
                    case VoiceSourceHelper.Event.NON_CHANGED:
                        String source = mVoiceSourceHelper.getUsbVoiceSource() == null ? VoiceSourceHelper.VOICE_SOURCE_ANDROID : VoiceSourceHelper.VOICE_SOURCE_USB;
                        DFServiceEvent serviceEvent = new DFServiceEvent(DFServiceEvent.ACTION_ON_USB_NON_CHANGED);
                        serviceEvent.putExtra(DFServiceEvent.PARAM_AUDIO_SOURCE, source);
                        serviceEvent.putExtra(DFServiceEvent.PARAM_IS_AUDIO_DATA_CORRECT, mDFServiceStatus.isAudioDataCorrect());
                        serviceEvent.send();
                        break;
                }
            }
        });
    }

    private void initDialogFlowService() {
        BackgroundThread.getHandler().post(new Runnable() {
            @Override
            public void run() {
                VoiceConfiguration config = DialogFlowConfig.getVoiceConfig(mContext, mVoiceSourceHelper.getUsbVoiceSource());
                mDFServiceStatus.setUsbDeviceAvailable(mVoiceSourceHelper.getUsbVoiceSource() != null);
                mDialogFlowService = DialogFlowService.queryService(mContext, config, mServiceCallback, mAgentQueryStatus);
                registerScenes();
            }
        });
    }

    private void registerScenes() {
        // Register all scenes from scene mangers
        mSceneManagers.add(new TelephonySceneManager(mContext, mDialogFlowService));
        mSceneManagers.add(new NaviSceneManager(mContext, mDialogFlowService));
        mSceneManagers.add(new SceneStopIntentManager(mContext, mDialogFlowService, KikaGoActivity.class));
        mSceneManagers.add(new SmsSceneManager(mContext, mDialogFlowService));
        mSceneManagers.add(new IMSceneManager(mContext, mDialogFlowService));
        mSceneManagers.add(new CommonSceneManager(mContext, mDialogFlowService));
        mSceneManagers.add(new GotoMainSceneManager(mContext, mDialogFlowService));
        mSceneManagers.add(new MusicSceneManager(mContext, mDialogFlowService));
        mSceneManagers.add(new ErrorSceneManager(mContext, mDialogFlowService));
        mCloseSceneManager = new CloseSceneManager(mContext, mDialogFlowService);
        mHelpSceneManager = new HelpSceneManager(mContext, mDialogFlowService);
        mSceneManagers.add(mCloseSceneManager);
        mSceneManagers.add(mHelpSceneManager);
    }

    private void unregisterScenes() {
        for (BaseSceneManager bcm : mSceneManagers) {
            if (bcm != null) bcm.close();
        }
    }

    private void setupDialogFlowService() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "setupDialogFlowService");
        }
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

    public void updateVoiceSource() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("updateVoiceSource, source: %s", mVoiceSourceHelper.getUsbVoiceSource()));
        }
        if (mDialogFlowService != null) {
            BackgroundThread.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (mAsrMaxDurationTimer.isCounting()) {
                        mAsrMaxDurationTimer.stop();
                    }
                    KikaGoVoiceSource usbSource = mVoiceSourceHelper.getUsbVoiceSource();
                    VoiceConfiguration config = DialogFlowConfig.getVoiceConfig(mContext, usbSource);
                    mDFServiceStatus.setUsbDeviceAvailable(usbSource != null);
                    mDFServiceStatus.setAudioDataCorrect(true);
                    mDFServiceStatus.setInit(false);
                    mDialogFlowService.updateRecorderSource(config);
                }
            });
        }
    }


    public void doOnReceiveNewMsg(String msgCommend, long msgTimestamp) {
        final boolean isServiceAwake = mDFServiceStatus.isAwake();
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("isServiceAwake: %s", isServiceAwake));
        }
        if (mDialogFlowService != null) {
            if (!isServiceAwake) {
                doOnNewMsg(msgCommend, msgTimestamp);
            } else {
                mMsgQueue.offer(new MsgTask(msgCommend, msgTimestamp));
            }
        }
    }

    private void doOnNewMsg(String msgCommend, long msgTimestamp) {
        if (mDialogFlowService != null) {
            mDialogFlowService.wakeUp(SceneReplyIM.SCENE);
            mDialogFlowService.resetContexts();
            mDialogFlowService.talk(String.format(Locale.ENGLISH, msgCommend, msgTimestamp), false);
        }
    }

    public void doOnScreenUnlock() {
        if (mDialogFlowService != null && mDFServiceStatus.isInit()) {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "enable WakeUp Detector");
            }
            mDialogFlowService.enableWakeUpDetector();
        }
    }

    public void doOnScreenLock() {
        if (mDialogFlowService != null && mDFServiceStatus.isInit()) {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "disable WakeUp Detector");
            }
            mDialogFlowService.sleep();
            mDialogFlowService.disableWakeUpDetector();
        }
    }


    public void talk(String text, boolean proactive) {
        cancelAsr();
        if (mDialogFlowService != null) {
            mDialogFlowService.cancelAsrAlignment();
            mDialogFlowService.talk(text, proactive);
        }
    }

    public void wakeUp(String wakeupFrom) {
        if (mDialogFlowService != null) {
            mDialogFlowService.wakeUp(wakeupFrom);
        }
    }

    public void enableWakeUpDetector() {
        if (mDialogFlowService != null) {
            mDialogFlowService.enableWakeUpDetector();
        }
    }

    public void disableWakeUpDetector() {
        if (mDialogFlowService != null) {
            mDialogFlowService.disableWakeUpDetector();
        }
    }

    public void scanUsbDevices() {
        mVoiceSourceHelper.scanUsbDevices(mContext);
    }

    public void usbVolumeUp() {
        if (mVoiceSourceHelper != null) {
            mVoiceSourceHelper.usbVolumeDown();
        }
    }

    public void usbVolumeDown() {
        if (mVoiceSourceHelper != null) {
            mVoiceSourceHelper.usbVolumeUp();
        }
    }

    public void quitService() {
        unregisterScenes();
        if (mDialogFlowService != null) {
            mDialogFlowService.quitService();
        }
        mVoiceSourceHelper.closeDevice(mContext);
    }

    public KikaGoVoiceSource getUsbVoiceSource() {
        return mVoiceSourceHelper.getUsbVoiceSource();
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

    private synchronized void cancelAsr() {
        if (mDialogFlowService != null) {
            mDialogFlowService.cancelListening();
            if (mAsrMaxDurationTimer.isCounting()) {
                mAsrMaxDurationTimer.stop();
            }
        }
    }


    private class MsgTask {
        private String commend;
        private long timestamp;

        private MsgTask(String commend, long timestamp) {
            this.commend = commend;
            this.timestamp = timestamp;
        }
    }
}
