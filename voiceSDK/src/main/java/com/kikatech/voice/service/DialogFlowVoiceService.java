package com.kikatech.voice.service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.EmojiRecommendMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.EmojiUtil;
import com.kikatech.voice.util.log.LogUtil;

/**
 * Created by brad_chang on 2017/12/29.
 */

public abstract class DialogFlowVoiceService {

    private static final String TAG = "DialogFlowVoiceService";

    final Context mContext;
    final IDialogFlowService.IServiceCallback mServiceCallback;

    private final AsrConfiguration mAsrConfiguration = new AsrConfiguration.Builder().build();
    VoiceService mVoiceService;

    DialogFlowVoiceService(@NonNull Context ctx, @NonNull IDialogFlowService.IServiceCallback callback) {
        mContext = ctx;
        mServiceCallback = callback;

        Message.register(Message.MSG_TYPE_INTERMEDIATE, IntermediateMessage.class);
        Message.register(Message.MSG_TYPE_ALTER, EditTextMessage.class);
        Message.register(Message.MSG_TYPE_ASR, TextMessage.class);
        Message.register(Message.MSG_TYPE_EMOJI, EmojiRecommendMessage.class);
    }

    void initVoiceService(@NonNull VoiceConfiguration conf) {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        AsrConfiguration asrConfig = conf.getConnectionConfiguration().getAsrConfiguration();
        mAsrConfiguration.copyConfig(asrConfig);
        mVoiceService = VoiceService.getService(mContext, conf);

        mVoiceService.setVoiceActiveStateListener(mVoiceActiveStateListener);
        mVoiceService.setVoiceRecognitionListener(mVoiceRecognitionListener);
        mVoiceService.setVoiceStateChangedListener(mVoiceStateChangedListener);

        mVoiceService.create();
        if (LogUtil.DEBUG) LogUtil.log(TAG, "idle VoiceService ... Done");
    }

    void updateAsrConfig(AsrConfiguration asrConfig) {
        if (mVoiceService != null && mAsrConfiguration.update(asrConfig)) {
            mVoiceService.updateAsrSettings(mAsrConfiguration);
            if (LogUtil.DEBUG) {
                mServiceCallback.onAsrConfigChange(mAsrConfiguration);
            }
        }
    }
    
    private final VoiceService.VoiceActiveStateListener mVoiceActiveStateListener = new VoiceService.VoiceActiveStateListener() {
        @Override
        public void onWakeUp() {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onWakeUp");
            }
            onVoiceWakeUp();
        }

        @Override
        public void onSleep() {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onSleep");
            }
            onVoiceSleep();
        }
    };

    abstract void onVoiceSleep();

    abstract void onVoiceWakeUp();

    private final VoiceService.VoiceRecognitionListener mVoiceRecognitionListener = new VoiceService.VoiceRecognitionListener() {
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

            onAsrResult(query, emojiJson, queryDialogFlow, nBestQuery);
        }
    };

    abstract void onAsrResult(String query, String emojiJson, boolean queryDialogFlow, String[] nBestQuery);

    void quitVoiceService() {
        if (mVoiceService != null) {
            mVoiceService.stop();
            mVoiceService.destroy();
        }

        Message.unregisterAll();
    }

    private final VoiceService.VoiceStateChangedListener mVoiceStateChangedListener = new VoiceService.VoiceStateChangedListener() {

        @Override
        public void onCreated() {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceState] onCreated, mVoiceService:" + mVoiceService);
            if (mVoiceService != null) {
                mVoiceService.start();
            }

            mServiceCallback.onInitComplete();
            mServiceCallback.onAsrConfigChange(mAsrConfiguration);
            mServiceCallback.onRecorderSourceUpdate();
            mServiceCallback.onConnectionStatusChange(IDialogFlowService.IServiceCallback.CONNECTION_STATUS_OPENED);
        }

        @Override
        public void onStartListening() {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceState] onStartListening");
        }

        @Override
        public void onStopListening() {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceState] onStopListening");
        }

        @Override
        public void onDestroyed() {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "[VoiceState] onDestroyed");
            }
        }

        @Override
        public void onError(int reason) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "[VoiceState] onError : " + reason);
            }
            mServiceCallback.onConnectionStatusChange(IDialogFlowService.IServiceCallback.CONNECTION_STATUS_ERR_DISCONNECT);
        }

        @Override
        public void onVadBos() {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "[VoiceState] onVadBos");
            }
            mServiceCallback.onVadBos();
        }

        @Override
        public void onConnectionClosed() {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "[VoiceState] onConnectionClosed");
            }

            mServiceCallback.onConnectionStatusChange(IDialogFlowService.IServiceCallback.CONNECTION_STATUS_CLOSED);
        }
    };
}
