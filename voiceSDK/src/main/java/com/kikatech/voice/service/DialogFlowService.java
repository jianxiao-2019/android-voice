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
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.EmojiRecommendMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.AsyncThread;
import com.kikatech.voice.util.EmojiUtil;
import com.kikatech.voice.util.log.LogUtil;


/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowService implements
        IDialogFlowService,
        VoiceService.VoiceActiveStateListener,
        VoiceService.VoiceRecognitionListener, VoiceService.VoiceStateChangedListener {

    private static final String TAG = "DialogFlowService";

    private Context mContext;

    private final IServiceCallback mCallback;
    private final IAgentQueryStatus mQueryStatusCallback;

    private DialogFlow mDialogFlow;
    private boolean mQueryAnyWords = false;

    private VoiceService mVoiceService;
    private final AsrConfiguration mAsrConfiguration = new AsrConfiguration.Builder().build();

    private SceneManager mSceneManager;

    private TtsSource mTtsSource;

    private DialogFlowService(@NonNull Context ctx, @NonNull VoiceConfiguration conf,
                              @NonNull IServiceCallback callback,
                              @NonNull IAgentQueryStatus queryStatus) {
        mContext = ctx;
        mCallback = callback;
        mQueryStatusCallback = queryStatus;
        mSceneManager = new SceneManager(mSceneCallback, mSceneQueryWordsStatusCallback);
        initDialogFlow(conf);
        initVoiceService(conf);
        initTts();

        Message.register(Message.MSG_TYPE_INTERMEDIATE, IntermediateMessage.class);
        Message.register(Message.MSG_TYPE_ALTER, EditTextMessage.class);
        Message.register(Message.MSG_TYPE_ASR, TextMessage.class);
        Message.register(Message.MSG_TYPE_EMOJI, EmojiRecommendMessage.class);

        callback.onInitComplete();
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

    private void initVoiceService(@NonNull VoiceConfiguration conf) {
        AsrConfiguration asrConfig = conf.getConnectionConfiguration().getAsrConfiguration();
        mAsrConfiguration.copyConfig(asrConfig);
        mVoiceService = VoiceService.getService(mContext, conf);
        mVoiceService.setVoiceActiveStateListener(this);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.setVoiceStateChangedListener(this);
        mVoiceService.create();
        if (LogUtil.DEBUG) LogUtil.log(TAG, "idle VoiceService ... Done");
    }

    private void initTts() {
        if (mTtsSource == null) {
            mTtsSource = TtsService.getInstance().getSpeaker(TtsService.TtsSourceType.KIKA_WEB);
            mTtsSource.init(mContext, null);
            mTtsSource.setTtsStateChangedListener(mTtsListener);
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
        if (mDialogFlow != null) {
            mDialogFlow.resetContexts();
        }
        if (mSceneManager != null) {
            mSceneManager.exitCurrentScene();
        }
    }

    @Override
    public void wakeUp() {
        if (mVoiceService != null) {
            mVoiceService.wakeUp();
        }
    }

    @Override
    public void sleep() {
        if (mVoiceService != null) {
            mVoiceService.sleep();
        }
    }

    @Override
    public void talk(String words) {
        stopTts(true);
        if (mDialogFlow != null && !TextUtils.isEmpty(words)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "talk : " + words);
            mDialogFlow.talk(words, null, mQueryAnyWords ? QUERY_TYPE_LOCAL : QUERY_TYPE_SERVER, mQueryStatusCallback);
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
        if (mVoiceService != null) {
            mVoiceService.pauseAsr();
            if (mCallback != null) {
                mCallback.onASRPause();
            }
        }
    }

    @Override
    public void resumeAsr() {
        if (mVoiceService != null) {
            mVoiceService.resumeAsr();
            if (mCallback != null) {
                mCallback.onASRResume();
            }
        }
    }

    @Override
    public void quitService() {
        if (mVoiceService != null) {
            mVoiceService.stop();
            mVoiceService.destroy();
        }
        if (mTtsSource != null) {
            mTtsSource.close();
            mTtsSource = null;
        }

        Message.unregisterAll();
    }

    @Override
    public void onWakeUp() {
        if (mCallback != null) {
            mCallback.onWakeUp();
        }
    }

    @Override
    public void onSleep() {
        if (mCallback != null) {
            mCallback.onSleep();
        }
        if (mSceneManager != null) {
            mSceneManager.exitCurrentScene();
        }
    }

    @Override
    public void onRecognitionResult(Message message) {
        if (LogUtil.DEBUG && !(message instanceof IntermediateMessage)) {
            LogUtil.logd(TAG, "onMessage message = " + message);
        }

        boolean queryDialogFlow = false;
        String query = "";
        String[] nBestQuery = null;
        String emojiJson = "";

        if (message instanceof IntermediateMessage) {
            IntermediateMessage intermediateMessage = (IntermediateMessage) message;
            query = intermediateMessage.text;
        } else if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "Speech spoken" + "[done]" + " : " + textMessage.text);
            }

            query = textMessage.text[0];
            nBestQuery = textMessage.text;
            queryDialogFlow = true;
        } else if (message instanceof EditTextMessage) {
            String alter = ((EditTextMessage) message).altered;
            if (LogUtil.DEBUG) LogUtil.logd(TAG, "EditTextMessage altered = " + alter);

            query = alter;
            queryDialogFlow = true;
        } else if (message instanceof EmojiRecommendMessage) {
            EmojiRecommendMessage emoji = ((EmojiRecommendMessage) message);
            emojiJson = EmojiUtil.composeJsonString(emoji.emoji, emoji.descriptionText);
            if (LogUtil.DEBUG) LogUtil.logd(TAG, "EmojiRecommendMessage = " + emojiJson);
        }

        if (!TextUtils.isEmpty(query)) {
            mCallback.onASRResult(query, emojiJson, queryDialogFlow);
            if (queryDialogFlow && mDialogFlow != null) {
                mDialogFlow.talk(query, nBestQuery, mQueryAnyWords ? QUERY_TYPE_LOCAL : QUERY_TYPE_SERVER, mQueryStatusCallback);
            }
        } else if (!TextUtils.isEmpty(emojiJson)) {
            mDialogFlow.talk(emojiJson, nBestQuery, QUERY_TYPE_EMOJI, mQueryStatusCallback);
        }
    }

    @Override
    public void onCreated() {
        if (mVoiceService != null) {
            mVoiceService.start();
        }
    }

    @Override
    public void onStartListening() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "onStartListening");
    }

    @Override
    public void onStopListening() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "onStopListening");
    }

    @Override
    public void onDestroyed() {
    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {
        //if (LogUtil.DEBUG) LogUtil.log(TAG, "onSpeechProbabilityChanged:" + prob);
    }

    @Override
    public void onError(int reason) {
    }

    @Override
    public void onVadBos() {
        if (mCallback != null) {
            mCallback.onVadBos();
        }
    }

    public ISceneFeedback getTtsFeedback() {
        return mSceneFeedback;
    }

    private final ISceneFeedback mSceneFeedback = new ISceneFeedback() {
        @Override
        public void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras, ISceneStageFeedback feedback) {
            mCallback.onTextPairs(pairs, extras);
            tts(pairs, feedback);
        }

        @Override
        public void onText(String text, Bundle extras, ISceneStageFeedback feedback) {
            mCallback.onText(text, extras);
            tts(text, feedback);
        }

        @Override
        public void onStagePrepared(String scene, String action, SceneStage sceneStage) {
            mCallback.onStagePrepared(scene, action, sceneStage);
        }

        @Override
        public void onStageActionDone(boolean isInterrupted) {
            mCallback.onStageActionDone(isInterrupted);
        }

        @Override
        public void onStageEvent(Bundle extras) {
            mCallback.onStageEvent(extras);
        }
    };

    private TtsStateDispatchListener mTtsListener = new TtsStateDispatchListener();

    private final class TtsStateDispatchListener implements TtsSource.TtsStateChangedListener {
        private ISceneStageFeedback listener;

        private long DELAY = 500;

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
                AsyncThread.getIns().executeDelay(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(false);
                    }
                }, DELAY);
            }
        }

        @Override
        public void onTtsInterrupted() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsInterrupted");
            }
            final ISceneStageFeedback feedback = listener;
            if (feedback != null) {
                AsyncThread.getIns().executeDelay(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(true);
                    }
                }, DELAY);
            }
        }

        @Override
        public void onTtsError() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsError");
            }
            final ISceneStageFeedback feedback = listener;
            if (feedback != null) {
                AsyncThread.getIns().executeDelay(new Runnable() {
                    @Override
                    public void run() {
                        feedback.onStageActionDone(true);
                    }
                }, DELAY);
            }
        }
    }

    private void updateAsrConfig(AsrConfiguration asrConfig) {
        if (mVoiceService != null && mAsrConfiguration.update(asrConfig)) {
            mVoiceService.updateAsrSettings(mAsrConfiguration);
        }
    }

    private SceneManager.SceneLifecycleObserver mSceneCallback = new SceneManager.SceneLifecycleObserver() {
        @Override
        public void doSleep(String scene) {
            stopTts(true);
            sleep();
        }

        @Override
        public void onSceneEnter(String scene) {
        }

        @Override
        public void onSceneExit(String scene, boolean proactive) {
            // if not proactive, Don't reset context since it would clear the context of the following scenario
            stopTts(false);
            if (proactive) {
                mDialogFlow.resetContexts();
                sleep();
            }
            mCallback.onSceneExit(proactive);
        }

        @Override
        public void onSceneStageAsrModeChange(AsrConfiguration asrConfig) {
            updateAsrConfig(asrConfig);
        }
    };

    private SceneManager.SceneQueryWordsStatus mSceneQueryWordsStatusCallback = new SceneManager.SceneQueryWordsStatus() {
        @Override
        public void onQueryAnyWordsStatusChange(boolean queryAnyWords) {
            mQueryAnyWords = queryAnyWords;
            if (LogUtil.DEBUG) LogUtil.log(TAG, "QueryAnyContent:" + mQueryAnyWords);
        }
    };
}