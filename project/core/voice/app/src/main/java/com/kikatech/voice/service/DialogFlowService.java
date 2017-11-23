package com.kikatech.voice.service;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogFlow;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneManager;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.tts.TtsSpeaker;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.util.log.LogUtil;


/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowService implements
        IDialogFlowService,
        VoiceService.VoiceRecognitionListener, VoiceService.VoiceStateChangedListener {

    private static final String TAG = "DialogFlowService";

    private Context mContext;

    private final IServiceCallback mCallback;
    private final IAgentQueryStatus mQueryStatusCallback;
    private VoiceService mVoiceService;
    private DialogFlow mDialogFlow;
    private boolean mQueryAnyContent = false;

    private SceneManager mSceneManager;

    private TtsSpeaker mTtsSpeaker;

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
        callback.onInitComplete();

        Message.register("INTERMEDIATE", IntermediateMessage.class);
        Message.register("ALTER", EditTextMessage.class);
        Message.register("ASR", TextMessage.class);
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
        mVoiceService = VoiceService.getService(mContext, conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.setVoiceStateChangedListener(this);
        mVoiceService.start();
        if (LogUtil.DEBUG) LogUtil.log(TAG, "idle VoiceService ... Done");
    }

    private void initTts() {
        if (mTtsSpeaker == null) {
            mTtsSpeaker = TtsService.getInstance().getSpeaker();
            mTtsSpeaker.init(mContext, null);
            mTtsSpeaker.setTtsStateChangedListener(mTtsListener);
        }
    }

    private void tts(String words, IDialogFlowFeedback.IToSceneFeedback listener) {
        if (mTtsSpeaker == null) {
            return;
        }
        try {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "tts, words: " + words);
            }
            if (mTtsSpeaker.isTtsSpeaking()) {
                mTtsSpeaker.interrupt();
                tts(words, listener);
            } else {
                mTtsListener.bindListener(listener);
                mTtsSpeaker.speak(words);
            }
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
    public void talk(String words) {
        if (mDialogFlow != null && !TextUtils.isEmpty(words)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "talk : " + words);
            mDialogFlow.talk(words, mQueryAnyContent, mQueryStatusCallback);
        }
    }

    @Override
    public void text(String words) {
        if (mDialogFlow != null && !TextUtils.isEmpty(words)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "text : " + words);
            mDialogFlow.talk(words, mQueryAnyContent, mQueryStatusCallback);
        }
    }

    @Override
    public void quitService() {
        if (mVoiceService != null) {
            mVoiceService.stop();
        }
        if (mTtsSpeaker != null) {
            mTtsSpeaker.close();
            mTtsSpeaker = null;
        }

        Message.unregisterAll();
    }

//    mVoiceService.sendCommand(SERVER_COMMAND_CONTENT, mEditText.getText().toString());

    @Override
    public void onRecognitionResult(Message message) {
        if (LogUtil.DEBUG && !(message instanceof IntermediateMessage)) {
            LogUtil.logd(TAG, "onMessage message = " + message);
        }
        if (message instanceof EditTextMessage) {
            String alter = ((EditTextMessage) message).context;
            if (LogUtil.DEBUG) LogUtil.logd(TAG, "EditTextMessage original = " + alter);
        }

        if (message instanceof IntermediateMessage) {
            IntermediateMessage intermediateMessage = (IntermediateMessage) message;
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "Speech spoken" + " : " + intermediateMessage.text);
            }

            mCallback.onASRResult(intermediateMessage.text, false);
        }
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "Speech spoken" + "[done]" + " : " + textMessage.text);
            }

            if (mDialogFlow != null) {
                mDialogFlow.talk(textMessage.text, mQueryAnyContent, mQueryStatusCallback);
            }
            mCallback.onASRResult(textMessage.text, true);
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
    public void onSpeechProbabilityChanged(float prob) {
        //if (LogUtil.DEBUG) LogUtil.log(TAG, "onSpeechProbabilityChanged:" + prob);
    }

    public ISceneFeedback getTtsFeedback() {
        return mSceneFeedback;
    }

    private final ISceneFeedback mSceneFeedback = new ISceneFeedback() {
        @Override
        public void onText(String text, Bundle extras, IDialogFlowFeedback.IToSceneFeedback feedback) {
            mCallback.onText(text, extras);
            tts(text, feedback);
        }

        @Override
        public void onStagePrepared(String scene, String action, SceneStage sceneStage) {
            mCallback.onStagePrepared(scene, action, sceneStage);
        }

        @Override
        public void onStageActionDone(boolean isEndOfScene, boolean isInterrupted) {
            mCallback.onStageActionDone(isEndOfScene, isInterrupted);
        }
    };

    private TtsStateDispatchListener mTtsListener = new TtsStateDispatchListener();

    private final class TtsStateDispatchListener implements TtsSpeaker.TtsStateChangedListener {
        private IDialogFlowFeedback.IToSceneFeedback listener;

        private void bindListener(IDialogFlowFeedback.IToSceneFeedback listener) {
            this.listener = listener;
        }

        @Override
        public void onTtsStart() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsStart");
            }
            if (listener != null) {
                listener.onTtsStart();
            }
        }

        @Override
        public void onTtsComplete() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsComplete");
            }
            if (listener != null) {
                listener.onTtsComplete();
            }
        }

        @Override
        public void onTtsInterrupted() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsInterrupted");
            }
            if (listener != null) {
                listener.onTtsInterrupted();
            }
        }

        @Override
        public void onTtsError() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsError");
            }
            if (listener != null) {
                listener.onTtsError();
            }
        }
    }

    private SceneManager.SceneLifecycleObserver mSceneCallback = new SceneManager.SceneLifecycleObserver() {
        @Override
        public void onSceneEnter(String scene) {
        }

        @Override
        public void onSceneExit(String scene) {
            mDialogFlow.resetContexts();
        }
    };

    private SceneManager.SceneQueryWordsStatus mSceneQueryWordsStatusCallback = new SceneManager.SceneQueryWordsStatus() {
        @Override
        public void onQueryAnyWordsStatusChange(boolean queryAnyWords) {
            mQueryAnyContent = queryAnyWords;
            if (LogUtil.DEBUG) LogUtil.log(TAG, "QueryAnyContent:" + mQueryAnyContent);
        }
    };
}