package com.kikatech.voice.service.dialogflow;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.EmojiRecommendMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.EmojiUtil;
import com.kikatech.voice.util.log.Logger;

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
    }

    void initVoiceService(@NonNull VoiceConfiguration conf) {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        AsrConfiguration asrConfig = conf.getConnectionConfiguration().getAsrConfiguration();
        mAsrConfiguration.copyConfig(asrConfig);
        mVoiceService = VoiceService.getService(mContext, conf);

        mVoiceService.setVoiceWakeUpListener(mVoiceWakeUpListener);
        mVoiceService.setVoiceRecognitionListener(mVoiceRecognitionListener);

        mVoiceService.create();
        if (Logger.DEBUG) Logger.i(TAG, "idle VoiceService ... Done");
    }

    void updateAsrConfig(AsrConfiguration asrConfig) {
        if (mVoiceService != null && mAsrConfiguration.update(asrConfig)) {
            mVoiceService.updateAsrSettings(mAsrConfiguration);
            if (Logger.DEBUG) {
                mServiceCallback.onAsrConfigChange(mAsrConfiguration);
            }
        }
    }

    private final VoiceService.VoiceWakeUpListener mVoiceWakeUpListener = new VoiceService.VoiceWakeUpListener() {
        @Override
        public void onWakeUp() {
            if (Logger.DEBUG) {
                Logger.i(TAG, "onWakeUp");
            }
            onVoiceWakeUp();
        }

        @Override
        public void onSleep() {
            if (Logger.DEBUG) {
                Logger.i(TAG, "onSleep");
            }
            onVoiceSleep();
        }
    };

    abstract void onVoiceSleep();

    abstract void onVoiceWakeUp();

    abstract void onAsrResult(String query, String emojiJson, boolean queryDialogFlow, String[] nBestQuery);

    void quitVoiceService() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
        }
    }

    private final VoiceService.VoiceRecognitionListener mVoiceRecognitionListener = new VoiceService.VoiceRecognitionListener() {

        public void onCreated() {
            if (Logger.DEBUG) {
                Logger.i(TAG, "[VoiceState] onCreated, mVoiceService:" + mVoiceService);
            }
            if (mVoiceService != null) {
                mVoiceService.start();
            }

            mServiceCallback.onInitComplete();
            mServiceCallback.onAsrConfigChange(mAsrConfiguration);
            mServiceCallback.onRecorderSourceUpdate();
        }

        @Override
        public void onRecognitionResult(Message message) {
            performOnRecognitionResult(message);
        }

        private void performOnRecognitionResult(Message message) {
            if (Logger.DEBUG && !(message instanceof IntermediateMessage)) {
                Logger.d(TAG, "onMessage message = " + message);
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
                if (Logger.DEBUG) {
                    Logger.i(TAG, "Speech spoken" + "[done]" + " : " + textMessage.text);
                }
                query = textMessage.text[0];
                nBestQuery = textMessage.text;
                queryDialogFlow = true;
            } else if (message instanceof EditTextMessage) {
                EditTextMessage editTextMessage = (EditTextMessage) message;
                String alter = editTextMessage.altered;
                if (Logger.DEBUG) {
                    Logger.d(TAG, "EditTextMessage altered = " + alter);
                }
                query = alter;
                queryDialogFlow = true;
            } else if (message instanceof EmojiRecommendMessage) {
                EmojiRecommendMessage emoji = ((EmojiRecommendMessage) message);
                emojiJson = EmojiUtil.composeJsonString(emoji.emoji, emoji.descriptionText);
                if (Logger.DEBUG) Logger.d(TAG, "EmojiRecommendMessage = " + emojiJson);
            }

            onAsrResult(query, emojiJson, queryDialogFlow, nBestQuery);
        }

        @Override
        public void onError(int reason) {
            if (Logger.DEBUG) {
                Logger.i(TAG, "[VoiceState] onError : " + reason);
            }
            mServiceCallback.onError(reason);
        }
    };
}
