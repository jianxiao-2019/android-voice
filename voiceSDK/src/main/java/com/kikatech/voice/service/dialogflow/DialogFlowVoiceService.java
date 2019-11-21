package com.kikatech.voice.service.dialogflow;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.voice.core.webservice.message.AlterMessage;
import com.kikatech.voice.core.webservice.message.EmojiRecommendMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.util.EmojiUtil;
import com.kikatech.voice.util.log.LogUtils;

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

            LogUtils.i(TAG, "onWakeUp");

            onVoiceWakeUp(mWakeupFrom);
        }

        @Override
        public void onSleep() {

            LogUtils.i(TAG, "onSleep");

            onVoiceSleep();
        }
    };

    private final VoiceService.VoiceRecognitionListener mVoiceRecognitionListener = new VoiceService.VoiceRecognitionListener() {
        @Override
        public void onRecognitionResult(Message message) {
            performOnRecognitionResult(message);
        }

        private void performOnRecognitionResult(Message message) {
            if (!(message instanceof IntermediateMessage)) {
                LogUtils.d(TAG, "onMessage message = " + message);
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

                LogUtils.i(TAG, "Speech spoken" + "[done]" + " : " + textMessage.text);

                asrResult = textMessage.text[0];
                asrNbestResult = textMessage.text;
                queryDialogFlow = true;
            } else if (message instanceof AlterMessage) {
                AlterMessage editTextMessage = (AlterMessage) message;
                String alter = editTextMessage.altered;

                LogUtils.d(TAG, "EditTextMessage altered = " + alter);

                asrResult = alter;
                queryDialogFlow = true;
            } else if (message instanceof EmojiRecommendMessage) {
                EmojiRecommendMessage emoji = ((EmojiRecommendMessage) message);
                emojiJson = EmojiUtil.composeJsonString(emoji.emoji, emoji.descriptionText);
                LogUtils.d(TAG, "EmojiRecommendMessage = " + emojiJson);
            }

            onAsrResult(asrResult, emojiJson, queryDialogFlow, asrNbestResult);
        }

        @Override
        public void onError(int reason) {

            LogUtils.i(TAG, "[VoiceState] onError : " + reason);

            mServiceCallback.onError(reason);
        }
    };


    protected final Context mContext;
    final IDialogFlowService.IServiceCallback mServiceCallback;

    private final AsrConfiguration mAsrConfiguration = new AsrConfiguration.Builder().build();
    private VoiceService mVoiceService;

    private String mWakeupFrom = "";


    DialogFlowVoiceService(@NonNull Context ctx, @NonNull IDialogFlowService.IServiceCallback callback) {

        LogUtils.i(TAG, "DialogFlowVoiceService constructor");

        mContext = ctx;
        mServiceCallback = callback;
    }


    void initVoiceService(@NonNull VoiceConfiguration conf) {

        LogUtils.i(TAG, "DialogFlowVoiceService initVoiceService");

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


        LogUtils.i(TAG, "DialogFlowVoiceService initVoiceService ... Done");

    }

    void updateAsrConfig(AsrConfiguration asrConfig) {
        if (mVoiceService != null && mAsrConfiguration.update(asrConfig)) {
            mVoiceService.updateAsrSettings(mAsrConfiguration);
            mServiceCallback.onAsrConfigChange(mAsrConfiguration);
        }
    }


    @Override
    public synchronized void wakeUp(String wakeupFrom) {

        LogUtils.i(TAG, "wakeupFrom " + wakeupFrom);

        mWakeupFrom = wakeupFrom;
        if (mVoiceService != null) {
            mVoiceService.wakeUp();
        }
    }

    @Override
    public synchronized void sleep() {

        LogUtils.i(TAG, "sleep");

        mWakeupFrom = "";
        if (mVoiceService != null) {
            mVoiceService.sleep();
        }
    }


    @Override
    public synchronized void setAsrAudioFilePath(String path, String fileName) {
        if (mVoiceService != null) {
            mVoiceService.setAsrAudioFilePath(path, fileName);
        }
    }


    @Override
    public synchronized void startListening() {

        LogUtils.i(TAG, "DialogFlowVoiceService startListening");

        if (mVoiceService != null) {
            mVoiceService.start();
        }

        LogUtils.i(TAG, "DialogFlowVoiceService startListening ... Done");

    }

    @Override
    public void startListening(int bosDuration) {

        LogUtils.i(TAG, String.format("DialogFlowVoiceService startListening, bosDuration: %s ", bosDuration));

        if (mVoiceService != null) {
            mVoiceService.start(bosDuration);
        }

        LogUtils.i(TAG, String.format("DialogFlowVoiceService startListening, bosDuration: %s ... Done", bosDuration));

    }

    @Override
    public synchronized void stopListening() {

        LogUtils.i(TAG, "DialogFlowVoiceService stopListening");

        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.NORMAL);
        }

        LogUtils.i(TAG, "DialogFlowVoiceService stopListening ... Done");

    }

    @Override
    public synchronized void completeListening() {

        LogUtils.i(TAG, "DialogFlowVoiceService completeListening");

        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.COMPLETE);
        }

        LogUtils.i(TAG, "DialogFlowVoiceService completeListening ... Done");

    }

    @Override
    public synchronized void cancelListening() {

        LogUtils.i(TAG, "DialogFlowVoiceService cancelListening");

        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.CANCEL);
        }

        LogUtils.i(TAG, "DialogFlowVoiceService cancelListening ... Done");
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

        LogUtils.i(TAG, "DialogFlowVoiceService releaseVoiceService");

        if (mVoiceService != null) {
            mVoiceService.stop(VoiceService.StopType.CANCEL);
            mVoiceService.destroy();
        }

        LogUtils.i(TAG, "DialogFlowVoiceService releaseVoiceService ... Done");

    }
}
