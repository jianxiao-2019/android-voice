package com.kikatech.go.tutorial;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.DialogFlowConfig;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.services.presenter.VoiceSourceHelper;
import com.kikatech.go.tutorial.dialogflow.SceneTutorial;
import com.kikatech.go.tutorial.dialogflow.TutorialSceneActions;
import com.kikatech.go.tutorial.dialogflow.TutorialSceneManager;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.MediaPlayerUtil;
import com.kikatech.go.util.timer.CountingTimer;
import com.kikatech.go.view.GoLayout;
import com.kikatech.go.view.UiTaskManager;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.dialogflow.DialogFlowService;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;
import com.kikatech.voice.service.voice.VoiceService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2018/5/10.
 */

public class TutorialManager {
    private static final String TAG = "TutorialManager";

    private IDialogFlowService mDialogFlowService;

    private final List<BaseSceneManager> mSceneManagers = new ArrayList<>();

    private UiTaskManager mUiManager;
    private VoiceSourceHelper mVoiceSourceHelper = new VoiceSourceHelper();
    private Context mContext;

    private CountingTimer mAskWakeUpTimer = new CountingTimer(DialogFlowConfig.BOS_DURATION_TUTORIAL, new CountingTimer.ICountingListener() {
        @Override
        public void onTimeTickStart() {
        }

        @Override
        public void onTimeTick(long millis) {
        }

        @Override
        public void onTimeTickEnd() {
            doAskWakeUp();
        }

        @Override
        public void onInterrupted(long stopMillis) {
        }
    });

    private IDialogFlowService.IServiceCallback mServiceCallback = new IDialogFlowService.IServiceCallback() {
        @Override
        public void onInitComplete() {
            doAskWakeUp();
        }

        @Override
        public void onWakeUp(String scene) {
            if (mAskWakeUpTimer.isCounting()) {
                mAskWakeUpTimer.stop();
            }
            mUiManager.dispatchWakeUp(SceneTutorial.SCENE);
            onLocalIntent(SceneTutorial.SCENE, TutorialSceneActions.ACTION_NAV_START);
            MediaPlayerUtil.playAlert(mContext, R.raw.alert_dot, null);
        }

        @Override
        public void onSleep() {
            mUiManager.dispatchDismissTutorialDialog();
            mUiManager.dispatchSleep();
            doAskWakeUp();
        }

        @Override
        public void onASRPause() {
        }

        @Override
        public void onASRResume() {
        }

        @Override
        public void onASRResult(final String speechText, String emojiUnicode, final boolean isFinished) {
            mUiManager.dispatchSpeechTask(speechText, isFinished);
        }

        @Override
        public void onError(int reason) {
            switch (reason) {
                case VoiceService.ERR_NO_SPEECH:
                    mDialogFlowService.talkUncaught();
                    break;
                default:
                    setupDialogFlowService();
                    break;
            }
        }

        @Override
        public void onText(String text, Bundle extras) {
            mUiManager.dispatchTtsTask(text, extras);
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
            mUiManager.dispatchTtsTask(text, extras);
        }

        @Override
        public void onStagePrepared(String scene, String action, SceneStage sceneStage) {
            mUiManager.dispatchStageTask(sceneStage);
        }

        @Override
        public void onStageActionStart(boolean supportAsrInterrupted) {
        }

        @Override
        public void onStageActionDone(boolean isInterrupted, Integer overrideAsrBos) {
            mUiManager.onStageActionDone(overrideAsrBos != null ? overrideAsrBos : 0);
        }

        @Override
        public void onStageEvent(Bundle extras) {
            final String scene, action;
            final String title, description;
            @TutorialUtil.StageActionType int type = extras.getInt(TutorialUtil.StageEvent.KEY_TYPE);
            switch (type) {
                case TutorialUtil.StageActionType.ALERT_DONE:
                    if (mTutorialListener != null) {
                        mTutorialListener.onLastStage();
                    }
                    title = extras.getString(TutorialUtil.StageEvent.KEY_UI_TITLE);
                    description = extras.getString(TutorialUtil.StageEvent.KEY_UI_DESCRIPTION);
                    mUiManager.dispatchTutorialDialogDone(title, description, new GoLayout.IOnTutorialDialogClickListener() {
                        @Override
                        public void onApply() {
                            if (mTutorialListener != null) {
                                mTutorialListener.onDone();
                            }
                        }
                    });
                    break;
                case TutorialUtil.StageActionType.ALERT:
                    final OptionList optionList = extras.getParcelable(TutorialUtil.StageEvent.KEY_OPTION_LIST);
                    final int index = extras.getInt(TutorialUtil.StageEvent.KEY_UI_INDEX);
                    title = extras.getString(TutorialUtil.StageEvent.KEY_UI_TITLE);
                    description = extras.getString(TutorialUtil.StageEvent.KEY_UI_DESCRIPTION);
                    scene = extras.getString(TutorialUtil.StageEvent.KEY_SCENE);
                    action = extras.getString(TutorialUtil.StageEvent.KEY_ACTION);
                    mUiManager.dispatchTutorialDialog(optionList, index, title, description, new GoLayout.IOnTutorialDialogClickListener() {
                        @Override
                        public void onApply() {
                            onLocalIntent(scene, action);
                        }
                    });
                    break;
                case TutorialUtil.StageActionType.ASR:
                    mUiManager.dispatchDismissTutorialDialog();
                    MediaPlayerUtil.playAlert(mContext, R.raw.alert_dot, new MediaPlayerUtil.IPlayStatusListener() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onStop() {
                            mDialogFlowService.startListening();
                        }
                    });
                    break;
            }
        }

        @Override
        public void onSceneExit(boolean proactive) {
        }

        @Override
        public void onAsrConfigChange(AsrConfiguration asrConfig) {
        }

        @Override
        public void onRecorderSourceUpdate() {
        }
    };

    private IDialogFlowService.IAgentQueryStatus mAgentQueryStatus = new IDialogFlowService.IAgentQueryStatus() {
        @Override
        public void onStart(boolean proactive) {
            mUiManager.dispatchAgentQueryStart(true);
        }

        @Override
        public void onComplete(String[] dbgMsg) {
        }

        @Override
        public void onError(Exception e) {
        }
    };

    private ITutorialListener mTutorialListener;

    public TutorialManager(final Context context, final GoLayout layout, final ITutorialListener listener) {
        mContext = context;
        mTutorialListener = listener;
        mUiManager = new UiTaskManager(layout, null);
        initVoiceSource();
        initDialogFlowService();
    }

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
                    case VoiceSourceHelper.Event.USB_DEVICE_ERROR:
                        mVoiceSourceHelper.closeDevice(mContext);
                        setupDialogFlowService();
                        break;
                    case VoiceSourceHelper.Event.NON_CHANGED:
                        break;
                }
            }
        });
    }

    private void initDialogFlowService() {
        BackgroundThread.getHandler().post(new Runnable() {
            @Override
            public void run() {
                KikaGoVoiceSource usbSource = mVoiceSourceHelper.getUsbVoiceSource();
                VoiceConfiguration config = DialogFlowConfig.getTutorialConfig(mContext, usbSource);
                mDialogFlowService = DialogFlowService.queryService(mContext, config, mServiceCallback, mAgentQueryStatus);
                mDialogFlowService.init();
                registerScenes();
            }
        });
    }

    private void setupDialogFlowService() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "setupDialogFlowService");
        }
        if (mDialogFlowService == null) {
            initDialogFlowService();
        } else {
            updateVoiceSource();
        }
    }

    private void updateVoiceSource() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("updateVoiceSource, source: %s", mVoiceSourceHelper.getUsbVoiceSource()));
        }
        if (mDialogFlowService != null) {
            BackgroundThread.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    unregisterScenes();
                    KikaGoVoiceSource usbSource = mVoiceSourceHelper.getUsbVoiceSource();
                    VoiceConfiguration config = DialogFlowConfig.getTutorialConfig(mContext, usbSource);
                    mDialogFlowService.updateRecorderSource(config);
                    registerScenes();
                }
            });
        }
    }

    private void registerScenes() {
        // Register all scenes from scene mangers
        mSceneManagers.add(new TutorialSceneManager(mContext, mDialogFlowService));
    }

    private void unregisterScenes() {
        for (BaseSceneManager bcm : mSceneManagers) {
            if (bcm != null) bcm.close();
        }
    }

    private void doAskWakeUp() {
        String content[] = TutorialUtil.getAskWakeUp(mContext);
        mDialogFlowService.disableWakeUpDetector();
        mUiManager.dispatchTutorialDialog(null, 1, content[0], content[1], new GoLayout.IOnTutorialDialogClickListener() {
            @Override
            public void onApply() {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "onApply");
                }
                mAskWakeUpTimer.start();
                mDialogFlowService.enableWakeUpDetector();
                mUiManager.dispatchDismissTutorialDialog();
            }
        });
    }

    private void onLocalIntent(String scene, String action) {
        boolean isServiceAvailable = mDialogFlowService != null;
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("scene: %s, action: %s, isServiceAvailable: %s", scene, action, isServiceAvailable));
        }
        if (isServiceAvailable) {
            mDialogFlowService.onLocalIntent(scene, action);
        }
    }

    public void quitService() {
        unregisterScenes();
        if (mDialogFlowService != null) {
            mDialogFlowService.quitService();
        }
        mUiManager.release();
    }

    public interface ITutorialListener {
        void onLastStage();

        void onDone();
    }
}
