package com.kikatech.voice.service.dialogflow;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.kikatech.voice.core.dialogflow.DialogFlow;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneStageFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneManager;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.util.AsyncThread;
import com.kikatech.voice.util.log.LogUtils;


/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowService extends DialogFlowVoiceService implements IDialogFlowService {

    private static final String TAG = "DialogFlowService";

    private VoiceConfiguration mConfig;

    private final IAgentQueryStatus mQueryStatusCallback;
    private final ITtsStatusCallback mTtsStatusCallback;

    private DialogFlow mDialogFlow;
    private boolean mQueryAnyWords = false;

    private SceneManager mSceneManager;

    private TtsSource mTtsSource;

    private DialogFlowService(@NonNull Context ctx, @NonNull VoiceConfiguration conf,
                              @NonNull IServiceCallback callback,
                              @NonNull IAgentQueryStatus queryStatus,
                              @NonNull ITtsStatusCallback ttsStatusCallback) {
        super(ctx, callback);

        LogUtils.i(TAG, "DialogFlowService constructor");

        mQueryStatusCallback = queryStatus;
        mTtsStatusCallback = ttsStatusCallback;
        mSceneManager = new SceneManager(mSceneCallback, mSceneQueryWordsStatusCallback);
        mConfig = conf;

        LogUtils.i(TAG, "DialogFlowService constructor ... Done");

    }

    @Override
    public void init() {

        LogUtils.i(TAG, "DialogFlowService init");

        initDialogFlow(mConfig);
        initVoiceService(mConfig);
        initTts(mConfig);
        mServiceCallback.onInitComplete();

        LogUtils.i(TAG, "DialogFlowService init ... Done");

    }

    @Override
    public void setTtsVolume(float volume) {
        if (mTtsSource != null) {
            mTtsSource.setVolume(volume);
        }
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

        LogUtils.i(TAG, "DialogFlowService initDialogFlow");

        mDialogFlow = new DialogFlow(mContext, conf);
        mDialogFlow.setObserver(mSceneManager);
        mDialogFlow.resetContexts();

        LogUtils.i(TAG, "DialogFlowService initDialogFlow ... Done");

    }

    private void initTts(@NonNull VoiceConfiguration conf) {
        if (mTtsSource == null) {
            mTtsSource = TtsService.getInstance().getSpeaker(conf.getTtsType());
            mTtsSource.init(mContext, null);
            mTtsSource.setTtsStateChangedListener(mTtsListener);
        }
    }

    private void tts(Pair<String, Integer>[] pairs, ISceneStageFeedback listener) {
        if (mTtsSource == null) {
            return;
        }
        try {
            for (Pair<String, Integer> pair : pairs) {
                LogUtils.v(TAG, "tts, words: " + pair.first);
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

            LogUtils.v(TAG, "tts, words: " + words);

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

            LogUtils.v(TAG, "stopTts");

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
            @NonNull IServiceCallback callback, @NonNull IAgentQueryStatus queryStatus,
            @NonNull ITtsStatusCallback ttsStatusCallback) {
        return new DialogFlowService(ctx, conf, callback, queryStatus, ttsStatusCallback);
    }

    @Override
    public void resetContexts() {

        LogUtils.i(TAG, "DialogFlowService resetContexts");

        mQueryAnyWords = false;
        cancelAsrAlignment();
        if (mDialogFlow != null) {
            mDialogFlow.resetContexts();
        }
        if (mSceneManager != null) {
            mSceneManager.exitCurrentScene();
        }

        LogUtils.i(TAG, "DialogFlowService resetContexts ... Done");

    }

    @Override
    public void talk(String words, boolean proactive) {
        stopTts(true);
        if (mDialogFlow != null && !TextUtils.isEmpty(words)) {
            LogUtils.i(TAG, "talk : " + words);
            mDialogFlow.talk(words, null, mQueryAnyWords ? QUERY_TYPE_LOCAL : QUERY_TYPE_SERVER, proactive, mQueryStatusCallback);
        }
    }

    @Override
    public void onLocalIntent(String scene, String action) {
        if (mSceneManager != null) {
            mSceneManager.onIntent(new Intent(scene, action));
        }
    }

    @Override
    public void talkUncaught() {
        if (mSceneManager != null) {
            mSceneManager.notifyUncaught();
        }
    }

    @Override
    public synchronized void startListening() {
        super.startListening();
        if (mServiceCallback != null) {
            mServiceCallback.onASRResume();
        }
    }

    @Override
    public synchronized void startListening(int bosDuration) {
        super.startListening(bosDuration);
        if (mServiceCallback != null) {
            mServiceCallback.onASRResume();
        }
    }

    @Override
    public synchronized void stopListening() {
        super.stopListening();
        if (mServiceCallback != null) {
            mServiceCallback.onASRPause();
        }
    }

    @Override
    public synchronized void completeListening() {
        super.completeListening();
        if (mServiceCallback != null) {
            mServiceCallback.onASRPause();
        }
    }

    @Override
    public synchronized void cancelListening() {
        super.cancelListening();
        if (mServiceCallback != null) {
            mServiceCallback.onASRPause();
        }
    }

    @Override
    public synchronized void quitService() {
        super.releaseVoiceService();

        LogUtils.i(TAG, "DialogFlowService quitService");

        if (mTtsSource != null) {
            mTtsSource.close();
            mTtsSource = null;
        }

        LogUtils.i(TAG, "DialogFlowService quitService ... Done");

    }

    @Override
    public synchronized void updateRecorderSource(VoiceConfiguration config) {

        LogUtils.i(TAG, "DialogFlowService updateRecorderSource");

        initVoiceService(config);
        // Voice is re-initialized, go back to the
        resetContexts();
        // Notify that the service is now sleeping
        mServiceCallback.onSleep();

        LogUtils.i(TAG, "DialogFlowService updateRecorderSource ... Done");

    }

    public ISceneFeedback getTtsFeedback() {
        return mSceneFeedback;
    }

    private final ISceneFeedback mSceneFeedback = new ISceneFeedback() {
        @Override
        public void onTtsVolumeChanged(float volume) {
            if (mTtsSource != null) {
                mTtsSource.setVolume(volume);
            }
        }

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
        public void onStageActionDone(boolean isInterrupted, Integer overrideAsrBos) {
            mServiceCallback.onStageActionDone(isInterrupted, overrideAsrBos);
        }

        @Override
        public void onStageEvent(Bundle extras) {
            mServiceCallback.onStageEvent(extras);
        }

        @Override
        public void onStageRequestAsrAlignment(String[] alignment) {
            requestAsrAlignment(alignment);
        }

        @Override
        public void onStageCancelAsrAlignment() {
            cancelAsrAlignment();
        }

    };

    private TtsStateDispatchListener mTtsListener = new TtsStateDispatchListener();

    @Override
    protected void onVoiceSleep() {

        LogUtils.i(TAG, "DialogFlowService onVoiceSleep");

        mServiceCallback.onSleep();
        if (mSceneManager != null) {
            mSceneManager.exitCurrentScene();
        }

        LogUtils.i(TAG, "DialogFlowService onVoiceSleep Done");

    }

    @Override
    protected void onVoiceWakeUp(String scene) {

        LogUtils.i(TAG, "DialogFlowService onVoiceWakeUp");

        mServiceCallback.onWakeUp(scene);

        LogUtils.i(TAG, "DialogFlowService onVoiceWakeUp Done");

    }

    @Override
    protected void onAsrResult(String query, String emojiJson, boolean queryDialogFlow, String[] nBestQuery) {

        LogUtils.d(TAG, String.format("query: %s, emojiJson: %s, queryDialogFlow: %s", query, emojiJson, queryDialogFlow));

        if (!TextUtils.isEmpty(query)) {
            mServiceCallback.onASRResult(query, emojiJson, queryDialogFlow);
            if (queryDialogFlow && mDialogFlow != null) {
                mDialogFlow.talk(query, nBestQuery, mQueryAnyWords ? QUERY_TYPE_LOCAL : QUERY_TYPE_SERVER, true, mQueryStatusCallback);
            }
        } else if (!TextUtils.isEmpty(emojiJson)) {
            mDialogFlow.talk(emojiJson, nBestQuery, QUERY_TYPE_EMOJI, true, mQueryStatusCallback);
        } else {
            mServiceCallback.onError(VoiceService.ERR_NO_SPEECH);
        }
    }

    private final class TtsStateDispatchListener implements TtsSource.TtsStateChangedListener {
        private ISceneStageFeedback listener;

        private void bindListener(ISceneStageFeedback listener) {
            this.listener = listener;
        }

        @Override
        public void onTtsStart() {

            LogUtils.v(TAG, "onTtsStart");

            if (listener != null) {
                listener.onStageActionStart();
            }
            mTtsStatusCallback.onStart();
        }

        @Override
        public void onTtsComplete() {

            LogUtils.v(TAG, "onTtsComplete");

            final ISceneStageFeedback feedback = listener;
            if (feedback != null) {
                AsyncThread.getIns().execute(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(false);
                    }
                });
                mTtsStatusCallback.onStop();
            }
        }

        @Override
        public void onTtsInterrupted() {

            LogUtils.v(TAG, "onTtsInterrupted");

            final ISceneStageFeedback feedback = listener;
            if (feedback != null) {
                AsyncThread.getIns().execute(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(true);
                    }
                });
                mTtsStatusCallback.onStop();
            }
        }

        @Override
        public void onTtsError() {

            LogUtils.v(TAG, "onTtsError");

            final ISceneStageFeedback feedback = listener;
            if (feedback != null) {
                AsyncThread.getIns().execute(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(true);
                    }
                });
            }
        }
    }

    private final SceneManager.SceneLifecycleObserver mSceneCallback = new SceneManager.SceneLifecycleObserver() {

        @Override
        public void onSceneEnter(String scene) {
            mServiceCallback.onSceneEntered(scene);
        }

        @Override
        public void onSceneExit(String scene, boolean proactive) {
            // if not proactive, Don't reset context since it would clear the context of the following scenario

            LogUtils.v(TAG, "scene:" + scene + ", proactive:" + proactive);

            mQueryAnyWords = false;
            cancelAsrAlignment();
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
            LogUtils.i(TAG, "QueryAnyContent:" + mQueryAnyWords);
        }
    };
}