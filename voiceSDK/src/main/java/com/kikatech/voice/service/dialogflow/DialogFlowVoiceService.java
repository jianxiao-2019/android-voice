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

abstract class DialogFlowVoiceService implements IDialogFlowVoiceService {
    private static final String TAG = "DialogFlowVoiceService";


    protected abstract void onVoiceSleep();

    protected abstract void onVoiceWakeUp(String scene);

    protected abstract void onAsrResult(String query, String emojiJson, boolean queryDialogFlow, String[] nBestQuery);


    private final VoiceService.VoiceWakeUpListener mVoiceWakeUpListener = new VoiceService.VoiceWakeUpListener() {
        @Override
        public void onWakeUp() {
            if (Logger.DEBUG) {
                Logger.i(TAG, "onWakeUp");
            }
            onVoiceWakeUp(mWakeupFrom);
        }

        @Override
        public void onSleep() {
            if (Logger.DEBUG) {
                Logger.i(TAG, "onSleep");
            }
            onVoiceSleep();
        }
    };

    private final VoiceService.VoiceRecognitionListener mVoiceRecognitionListener = new VoiceService.VoiceRecognitionListener() {
        @Override
        public void onRecognitionResult(Message message) {
            performOnRecognitionResult(message);
        }

        private void performOnRecognitionResult(Message message) {
            if (Logger.DEBUG && !(message instanceof IntermediateMessage)) {
                Logger.d(TAG, "onMessage message = " + message);
            }

            boolean queryDialogFlow = false;
            String asrResult = "";
            String[] asrNbestResult = null;
            String emojiJson = "";

            if (message instanceof IntermediateMessage) {
                IntermediateMessage intermediateMessage = (IntermediateMessage) message;
                asrResult = intermediateMessage.text;
            } else if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                if (Logger.DEBUG) {
                    Logger.i(TAG, "Speech spoken" + "[done]" + " : " + textMessage.text);
                }
                asrResult = textMessage.text[0];
                asrNbestResult = textMessage.text;
                queryDialogFlow = true;
            } else if (message instanceof EditTextMessage) {
                EditTextMessage editTextMessage = (EditTextMessage) message;
                String alter = editTextMessage.altered;
                if (Logger.DEBUG) {
                    Logger.d(TAG, "EditTextMessage altered = " + alter);
                }
                asrResult = alter;
                queryDialogFlow = true;
            } else if (message instanceof EmojiRecommendMessage) {
                EmojiRecommendMessage emoji = ((EmojiRecommendMessage) message);
                emojiJson = EmojiUtil.composeJsonString(emoji.emoji, emoji.descriptionText);
                if (Logger.DEBUG) Logger.d(TAG, "EmojiRecommendMessage = " + emojiJson);
            }

            onAsrResult(asrResult, emojiJson, queryDialogFlow, asrNbestResult);
        }

        @Override
        public void onError(int reason) {
            if (Logger.DEBUG) {
                Logger.i(TAG, "[VoiceState] onError : " + reason);
            }
            mServiceCallback.onError(reason);
        }
    };


    protected final Context mContext;
    final IDialogFlowService.IServiceCallback mServiceCallback;

    private final AsrConfiguration mAsrConfiguration = new AsrConfiguration.Builder().build();
    private VoiceService mVoiceService;

    private String mWakeupFrom = "";


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
        mVoiceService.start();

        mServiceCallback.onAsrConfigChange(mAsrConfiguration);
        mServiceCallback.onRecorderSourceUpdate();

        if (Logger.DEBUG) {
            Logger.i(TAG, "idle VoiceService ... Done");
        }
    }

    void updateAsrConfig(AsrConfiguration asrConfig) {
        if (mVoiceService != null && mAsrConfiguration.update(asrConfig)) {
            mVoiceService.updateAsrSettings(mAsrConfiguration);
            if (Logger.DEBUG) {
                mServiceCallback.onAsrConfigChange(mAsrConfiguration);
            }
        }
    }


    @Override
    public synchronized void wakeUp(String wakeupFrom) {
        if (Logger.DEBUG) {
            Logger.i(TAG, "wakeupFrom " + wakeupFrom);
        }
        mWakeupFrom = wakeupFrom;
        if (mVoiceService != null) {
            mVoiceService.wakeUp();
        }
    }

    @Override
    public synchronized void sleep() {
        if (Logger.DEBUG) {
            Logger.i(TAG, "sleep");
        }
        mWakeupFrom = "";
        if (mVoiceService != null) {
            mVoiceService.sleep();
        }
    }


    @Override
    public synchronized void startListening() {
        if (mVoiceService != null) {
            mVoiceService.start();
        }
    }

    @Override
    public void startListening(int bosDuration) {
        if (mVoiceService != null) {
            mVoiceService.start(bosDuration);
        }
    }

    @Override
    public synchronized void stopListening() {
        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.NORMAL);
        }
    }

    @Override
    public synchronized void completeListening() {
        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.COMPLETE);
        }
    }

    @Override
    public synchronized void cancelListening() {
        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.CANCEL);
        }
    }


    @Override
    public synchronized void enableWakeUpDetector() {
        if (mVoiceService != null) {
            mVoiceService.setWakeUpDetectorEnable(true);
            startListening(-1);
        }
    }

    @Override
    public synchronized void disableWakeUpDetector() {
        if (mVoiceService != null) {
            mVoiceService.setWakeUpDetectorEnable(false);
            cancelListening();
        }
    }


    @Override
    public synchronized void requestAsrAlignment(String[] alignment) {
        if (mVoiceService != null) {
            mVoiceService.sendAlignment(alignment);
        }
    }

    @Override
    public synchronized void cancelAsrAlignment() {
        if (mVoiceService != null) {
            mVoiceService.cancelAlignment();
        }
    }


    @Override
    public synchronized void releaseVoiceService() {
        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.CANCEL);
            mVoiceService.destroy();
        }
    }
}
