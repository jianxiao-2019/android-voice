package com.kikatech.voice.service;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.kikatech.voice.core.dialogflow.DialogFlow;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneStageFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneManager;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.AsyncThread;
import com.kikatech.voice.util.log.LogUtil;


/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowService extends DialogFlowVoiceService implements IDialogFlowService {

    private static final String TAG = "DialogFlowService";


    private final IAgentQueryStatus mQueryStatusCallback;

    private DialogFlow mDialogFlow;
    private boolean mQueryAnyWords = false;
    private String mWakeupFrom = "";

    private SceneManager mSceneManager;

    private TtsSource mTtsSource;

    private DialogFlowService(@NonNull Context ctx, @NonNull VoiceConfiguration conf,
                              @NonNull IServiceCallback callback,
                              @NonNull IAgentQueryStatus queryStatus) {
        super(ctx, callback);

        mQueryStatusCallback = queryStatus;
        mSceneManager = new SceneManager(mSceneCallback, mSceneQueryWordsStatusCallback);
        initDialogFlow(conf);
        initVoiceService(conf);
        initTts(conf);
    }

    @Override
    public void registerScene(SceneBase scene) {
        mSceneManager.register(scene);
    }

    @Override
    public void unregisterScene(SceneBase scene) {
        mSceneManager.unregister(scene);
    }

    private void initDialogFlow(@NonNull VoiceConfiguration conf) {
        mDialogFlow = new DialogFlow(mContext, conf);
        mDialogFlow.setObserver(mSceneManager);
        mDialogFlow.resetContexts();
        if (LogUtil.DEBUG) LogUtil.log(TAG, "idle DialogFlow ... Done");
    }

    private void initTts(@NonNull VoiceConfiguration conf) {
        if (mTtsSource == null) {
            mTtsSource = TtsService.getInstance().getSpeaker(conf.getTtsType());
            mTtsSource.init(mContext, null);
            mTtsSource.setTtsStateChangedListener(mTtsListener);
        }
    }

    public void updateTtsSource(@NonNull VoiceConfiguration conf) {
        if (mTtsSource != null) {
            mTtsSource.close();
            mTtsSource = null;
        }
        initTts(conf);
    }

    @Override
    public void setWakeUpDetectorEnable(boolean enable) {
        if (mVoiceService != null) {
            mVoiceService.setWakeUpDetectorEnable(enable);
        }
    }

    private void tts(Pair<String, Integer>[] pairs, ISceneStageFeedback listener) {
        if (mTtsSource == null) {
            return;
        }
        try {
            if (LogUtil.DEBUG) {
                for (Pair<String, Integer> pair : pairs) {
                    LogUtil.logv(TAG, "tts, words: " + pair.first);
                }
            }
            if (mTtsSource.isTtsSpeaking()) {
                stopTts();
                tts(pairs, listener);
            } else {
                mTtsListener.bindListener(listener);
                mTtsSource.speak(pairs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tts(String words, ISceneStageFeedback listener) {
        if (mTtsSource == null) {
            return;
        }
        try {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "tts, words: " + words);
            }
            if (mTtsSource.isTtsSpeaking()) {
                stopTts();
                tts(words, listener);
            } else {
                mTtsListener.bindListener(listener);
                mTtsSource.speak(words);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopTts() {
        stopTts(false);
    }

    private void stopTts(boolean removeCallback) {
        if (mTtsSource == null) {
            return;
        }
        try {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "stopTts");
            }
            if (removeCallback) {
                mTtsListener.bindListener(null);
            }
            mTtsSource.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized IDialogFlowService queryService(
            @NonNull Context ctx, @NonNull VoiceConfiguration conf,
            @NonNull IServiceCallback callback, @NonNull IAgentQueryStatus queryStatus) {
        return new DialogFlowService(ctx, conf, callback, queryStatus);
    }

    @Override
    public void resetContexts() {
        mQueryAnyWords = false;
        if (mDialogFlow != null) {
            mDialogFlow.resetContexts();
        }
        if (mSceneManager != null) {
            mSceneManager.exitCurrentScene();
        }
    }

    @Override
    public void wakeUp(String wakeupFrom) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "wakeupFrom " + wakeupFrom);
        }
        mWakeupFrom = wakeupFrom;
        if (mVoiceService != null) {
            mVoiceService.wakeUp();
        }
    }

    @Override
    public void sleep() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "sleep");
        }
        mWakeupFrom = "";
        if (mVoiceService != null) {
            mVoiceService.sleep();
        }
    }

    @Override
    public void talk(String words, boolean proactive) {
        stopTts(true);
        if (mDialogFlow != null && !TextUtils.isEmpty(words)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "talk : " + words);
            mDialogFlow.talk(words, null, mQueryAnyWords ? QUERY_TYPE_LOCAL : QUERY_TYPE_SERVER, proactive, mQueryStatusCallback);
        }
    }

    @Override
    public void talkUncaught() {
        if (mSceneManager != null) {
            mSceneManager.notifyUncaught();
        }
    }

    @Override
    public void pauseAsr() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "pauseAsr");
        }
        if (mVoiceService != null) {
            mVoiceService.pauseAsr();
            if (mServiceCallback != null) {
                mServiceCallback.onASRPause();
            }
        }
    }

    @Override
    public void resumeAsr(int bosDuration) {
        if (mVoiceService != null) {
            mVoiceService.resumeAsr(bosDuration);
            if (mServiceCallback != null) {
                mServiceCallback.onASRResume();
            }
        }
    }

    @Override
    public void resumeAsr(boolean startBosNow) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "resumeAsr");
        }
        if (mVoiceService != null) {
            mVoiceService.resumeAsr(startBosNow);
            if (mServiceCallback != null) {
                mServiceCallback.onASRResume();
            }
        }
    }

    @Override
    public void cancelAsrAlignment() {
        if (mVoiceService != null) {
            mVoiceService.sendAlignment(new String[]{""});
        }
    }

    @Override
    public void quitService() {
        quitVoiceService();

        if (mTtsSource != null) {
            mTtsSource.close();
            mTtsSource = null;
        }
    }

    @Override
    public synchronized void updateRecorderSource(VoiceConfiguration config) {
        initVoiceService(config);
        // Voice is re-initialized, go back to the
        resetContexts();
        if (mServiceCallback != null) {
            mServiceCallback.onRecorderSourceUpdate();
            // Notify that the service is now sleeping
            mServiceCallback.onSleep();
        }
    }

    public ISceneFeedback getTtsFeedback() {
        return mSceneFeedback;
    }

    private final ISceneFeedback mSceneFeedback = new ISceneFeedback() {
        @Override
        public void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras, ISceneStageFeedback feedback) {
            mServiceCallback.onTextPairs(pairs, extras);
            tts(pairs, feedback);
        }

        @Override
        public void onText(String text, Bundle extras, ISceneStageFeedback feedback) {
            mServiceCallback.onText(text, extras);
            tts(text, feedback);
        }

        @Override
        public void onStagePrepared(String scene, String action, SceneStage sceneStage) {
            mServiceCallback.onStagePrepared(scene, action, sceneStage);
        }

        @Override
        public void onStageActionStart(boolean supportAsrInterrupted) {
            mServiceCallback.onStageActionStart(supportAsrInterrupted);
        }

        @Override
        public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume, Integer overrideAsrBos) {
            mServiceCallback.onStageActionDone(isInterrupted, delayAsrResume, overrideAsrBos);
        }

        @Override
        public void onStageEvent(Bundle extras) {
            mServiceCallback.onStageEvent(extras);
        }

        @Override
        public void onStageRequestAsrAlignment(String[] alignment) {
            if (mVoiceService != null) {
                mVoiceService.sendAlignment(alignment);
            }
        }

    };

    private TtsStateDispatchListener mTtsListener = new TtsStateDispatchListener();

    @Override
    void onVoiceSleep() {
        mServiceCallback.onSleep();
        if (mSceneManager != null) {
            mSceneManager.exitCurrentScene();
        }
    }

    @Override
    void onVoiceWakeUp() {
        mServiceCallback.onWakeUp(mWakeupFrom);
    }

    @Override
    void onAsrResult(String query, String emojiJson, boolean queryDialogFlow, String[] nBestQuery) {
        if (!TextUtils.isEmpty(query)) {
            mServiceCallback.onASRResult(query, emojiJson, queryDialogFlow);
            if (queryDialogFlow && mDialogFlow != null) {
                mDialogFlow.talk(query, nBestQuery, mQueryAnyWords ? QUERY_TYPE_LOCAL : QUERY_TYPE_SERVER, true, mQueryStatusCallback);
            }
        } else if (!TextUtils.isEmpty(emojiJson)) {
            mDialogFlow.talk(emojiJson, nBestQuery, QUERY_TYPE_EMOJI, true, mQueryStatusCallback);
        }
    }

    private final class TtsStateDispatchListener implements TtsSource.TtsStateChangedListener {
        private ISceneStageFeedback listener;

        private void bindListener(ISceneStageFeedback listener) {
            this.listener = listener;
        }

        @Override
        public void onTtsStart() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsStart");
            }
            if (listener != null) {
                listener.onStageActionStart();
            }
        }

        @Override
        public void onTtsComplete() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsComplete");
            }
            final ISceneStageFeedback feedback = listener;
            if (feedback != null) {
                AsyncThread.getIns().execute(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(false, true);
                    }
                });
            }
        }

        @Override
        public void onTtsInterrupted() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsInterrupted");
            }
            final ISceneStageFeedback feedback = listener;
            if (feedback != null) {
                AsyncThread.getIns().execute(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(true, true);
                    }
                });
            }
        }

        @Override
        public void onTtsError() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsError");
            }
            final ISceneStageFeedback feedback = listener;
            if (feedback != null) {
                AsyncThread.getIns().execute(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(true, true);
                    }
                });
            }
        }
    }

    private final SceneManager.SceneLifecycleObserver mSceneCallback = new SceneManager.SceneLifecycleObserver() {
        @Override
        public void onSceneEnter(String scene) {
        }

        @Override
        public void onSceneExit(String scene, boolean proactive) {
            // if not proactive, Don't reset context since it would clear the context of the following scenario
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "scene:" + scene + ", proactive:" + proactive);
            }
            mQueryAnyWords = false;
            stopTts(false);
            if (proactive) {
                mDialogFlow.resetContexts();
                sleep();
            }
            mServiceCallback.onSceneExit(proactive);
        }

        @Override
        public void onSceneStageAsrModeChange(AsrConfiguration asrConfig) {
            updateAsrConfig(asrConfig);
        }
    };

    private final SceneManager.SceneQueryWordsStatus mSceneQueryWordsStatusCallback = new SceneManager.SceneQueryWordsStatus() {
        @Override
        public void onQueryAnyWordsStatusChange(boolean queryAnyWords) {
            mQueryAnyWords = queryAnyWords;
            if (LogUtil.DEBUG) LogUtil.log(TAG, "QueryAnyContent:" + mQueryAnyWords);
        }
    };
}