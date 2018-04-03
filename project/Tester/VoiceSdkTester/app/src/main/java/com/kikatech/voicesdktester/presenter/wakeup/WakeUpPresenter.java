package com.kikatech.voicesdktester.presenter.wakeup;

import android.content.Context;
import android.os.Handler;

import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.service.VoiceService;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voicesdktester.utils.PreferenceUtil;

import java.io.File;

/**
 * Created by ryanlin on 02/04/2018.
 */

public abstract class WakeUpPresenter implements
        VoiceService.VoiceStateChangedListener,
        VoiceService.VoiceActiveStateListener {

    private static final String DEBUG_FILE_PATH = "voiceTesterWakeUp";

    private VoiceService mVoiceService;
    private AsrConfiguration mAsrConfiguration;

    protected IVoiceSource mVoiceSource;
    protected Context mContext;

    protected PresenterCallback mCallback;

    public WakeUpPresenter(Context context) {
        mContext = context;
    }

    public interface PresenterCallback {
        void onUpdateStatus(String status);
        void onReadyStateChanged(boolean ready);
        void onWakeUpResult(boolean success);
    }

    public void start() {
        if (mVoiceService != null) {
            mVoiceService.start();
        }
    }

    public void close() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
    }

    protected void attachService() {
        if (mVoiceService != null) {
            mVoiceService.destroy();
            mVoiceService = null;
        }
        // Debug
        AsrConfiguration.Builder builder = new AsrConfiguration.Builder();
        mAsrConfiguration = builder
                .setAlterEnabled(false)
                .setEmojiEnabled(false)
                .setPunctuationEnabled(false)
                .setSpellingEnabled(false)
                .setVprEnabled(false)
                .setEosPackets(9)
                .build();
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.setDebugFileTag(DEBUG_FILE_PATH);
        conf.setIsDebugMode(true);
        conf.setSupportWakeUpMode(true);
        conf.source(mVoiceSource);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName("KikaGoTest")
                .setUrl(PreferenceUtil.getString(
                        mContext,
                        PreferenceUtil.KEY_SERVER_LOCATION,
                        VoiceConfiguration.HostUrl.DEV_MVP))
                .setLocale("en_US")
                .setSign(RequestManager.getSign(mContext))
                .setUserAgent(RequestManager.generateUserAgent(mContext))
                .setEngine("google")
                .setAsrConfiguration(mAsrConfiguration)
                .build());
        mVoiceService = VoiceService.getService(mContext, conf);
        mVoiceService.setVoiceStateChangedListener(this);
        mVoiceService.setVoiceActiveStateListener(this);
        mVoiceService.create();
    }

    @Override
    public void onStartListening() {
        Logger.d("WakeUpTestActivity onStartListening");

        if (mCallback != null) {
            mCallback.onUpdateStatus("Start recording");
            mCallback.onReadyStateChanged(false);
        }

        mHandler.sendEmptyMessageDelayed(MSG_WAKE_UP_BOS, 3000);
    }

    @Override
    public void onStopListening() {
        Logger.d("WakeUpTestActivity onStopListening");
        if (mCallback != null) {
            mCallback.onUpdateStatus("Stop recording");
            mCallback.onReadyStateChanged(true);
        }
    }

    @Override
    public void onWakeUp() {
        Logger.d("onWakeUp");
        if (mCallback != null) {
            mCallback.onWakeUpResult(true);
        }

        mVoiceService.stop();
        mVoiceService.sleep();

        mHandler.removeMessages(MSG_WAKE_UP_BOS);
    }

    @Override
    public void onSleep() {
        Logger.d("onSleep");
    }

    private static final int MSG_WAKE_UP_BOS = 1;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WAKE_UP_BOS) {
                mVoiceService.stop();
                if (mCallback != null) {
                    mCallback.onWakeUpResult(false);
                }
            }
        }
    };

    protected void renameSuccessFile(final String path, final String pattern) {
        new Thread(() -> {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Logger.i("renameSuccessFile path = " + path);
            File oldFile = new File(path + pattern);
            Logger.i("renameSuccessFile oldFile = " + oldFile);
            File newFile = new File(path + "_s" + pattern);
            Logger.i("renameSuccessFile newFile = " + newFile);

            if (oldFile.exists() && !newFile.exists()) {
                oldFile.renameTo(newFile);
            }
        }).start();
    }

    public abstract void prepare();

    public void setPresenterCallback(PresenterCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onCreated() {

    }

    @Override
    public void onDestroyed() {

    }

    @Override
    public void onError(int reason) {

    }

    @Override
    public void onConnectionClosed() {

    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {

    }
}