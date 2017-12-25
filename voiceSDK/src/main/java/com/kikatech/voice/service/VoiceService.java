package com.kikatech.voice.service;

import android.content.Context;
import android.os.Handler;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.hotword.WakeUpDetector;
import com.kikatech.voice.core.ns.NoiseSuppression;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.recorder.VoiceRecorder;
import com.kikatech.voice.core.recorder.VoiceSource;
import com.kikatech.voice.core.vad.VoiceDetector;
import com.kikatech.voice.core.webservice.WebSocket;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;

import ai.kitt.snowboy.AppResCopy;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceService implements WakeUpDetector.OnHotWordDetectListener {

    public static final int REASON_NOT_CREATED = 1;
    private static final String SERVER_COMMAND_SETTINGS = "SETTINGS";

    private static final int HEARTBEAT_DURATION = 10 * 1000;

    private static final int MSG_VAD_BOS = 1;
    private static final int MSG_SEND_HEARTBEAT = 2;

    private VoiceConfiguration mConf;
    private WebSocket mWebService;

    private VoiceRecorder mVoiceRecorder;
    private VoiceDetector mVoiceDetector;
    private NoiseSuppression mNoiseSuppression;
    private WakeUpDetector mWakeUpDetector;

    private VoiceRecognitionListener mVoiceRecognitionListener;
    private VoiceStateChangedListener mVoiceStateChangedListener;
    private VoiceActiveStateListener mVoiceActiveStateListener;

    private Handler mMainThreadHandler;
    private TimerHandler mTimerHandler;

    private boolean mIsAsrPaused = false;

    @Override
    public void onDetected() {
        if (mVoiceActiveStateListener != null) {
            mVoiceActiveStateListener.onWakeUp();
        }
        startVadBosTimer();
    }

    public interface VoiceRecognitionListener {
        void onRecognitionResult(Message message);
    }

    public interface VoiceStateChangedListener {
        void onCreated();

        void onStartListening();

        void onStopListening();

        void onDestroyed();

        void onSpeechProbabilityChanged(float prob);

        void onError(int reason);

        void onVadBos();
    }

    public interface VoiceActiveStateListener {
        void onWakeUp();

        void onSleep();
    }

    private VoiceService(Context context, VoiceConfiguration conf) {
        mConf = conf;

        IVoiceSource voiceSource = mConf.getVoiceSource();
        boolean isUsbVoiceSource = false; // TODO : This will be move to outside.
        if (voiceSource == null) {
            voiceSource = new VoiceSource();
            mConf.source(voiceSource);
        } else {
            isUsbVoiceSource = true;
        }

        // TODO : base on the VoiceConfiguration.
        mVoiceDetector = new VoiceDetector(
                new FileWriter(mConf.getDebugFilePath() + "_speex", new VoiceDataSender()), new VoiceDetector.OnVadProbabilityChangeListener() {
            @Override
            public void onSpeechProbabilityChanged(final float speechProbability) {
                if (mMainThreadHandler != null) {
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mVoiceStateChangedListener != null) {
                                mVoiceStateChangedListener.onSpeechProbabilityChanged(speechProbability);
                            }
                        }
                    });
                }
            }
        });

        mNoiseSuppression = new NoiseSuppression(new FileWriter(mConf.getDebugFilePath() + "_NC", mVoiceDetector));

        IDataPath nextPipe = isUsbVoiceSource ? mNoiseSuppression : mVoiceDetector;
        String filePost = isUsbVoiceSource ? "_USB" : "";
        Logger.d("mConf.isSupportWakeUpMode() = " + mConf.isSupportWakeUpMode() + " [No NoiseSuppression]");
        Logger.d("isUsbVoiceSource = " + isUsbVoiceSource + " nextPipe = " + nextPipe);
        if (mConf.isSupportWakeUpMode()) {
            AppResCopy.copyResFromAssetsToSD(context);
            mWakeUpDetector = WakeUpDetector.getDetector(this, nextPipe, mConf.getDebugFilePath() + "_WD", voiceSource.isStereo());
            mVoiceRecorder = new VoiceRecorder(voiceSource, new FileWriter(mConf.getDebugFilePath() + filePost, mWakeUpDetector));
        } else {
            mVoiceRecorder = new VoiceRecorder(voiceSource, new FileWriter(mConf.getDebugFilePath() + filePost, nextPipe));
        }
    }

    public static VoiceService getService(Context context, VoiceConfiguration conf) {
        return new VoiceService(context, conf);
    }

    public void create() {
        if (mWebService != null) {
            mWebService.release();
        }

        mMainThreadHandler = new Handler();
        mTimerHandler = new TimerHandler();
        mTimerHandler.sendEmptyMessageDelayed(MSG_SEND_HEARTBEAT, HEARTBEAT_DURATION);

        mWebService = WebSocket.openConnection(mWebSocketListener);
        mWebService.connect(mConf.getConnectionConfiguration());
    }

    public void start() {
        Logger.d("VoiceService start mWebService.isConnecting() = " + mWebService.isConnecting());
        if (!mWebService.isConnecting()) {
            mVoiceStateChangedListener.onError(REASON_NOT_CREATED);
            return;
        }

        Logger.d("VoiceService start 2");
        if (mConf.isSupportWakeUpMode() && mWakeUpDetector == null) {
            mWakeUpDetector = WakeUpDetector.getDetector(this, mVoiceDetector, mConf.getDebugFilePath() + "_WD", mConf.getVoiceSource().isStereo());
        }
        if (mWakeUpDetector != null) {
            mWakeUpDetector.reset();
        }
        mVoiceDetector.startDetecting();
        mVoiceRecorder.start();

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onStartListening();
        }

        if (mWakeUpDetector == null || mWakeUpDetector.isAwake()) {
            startVadBosTimer();
        }
    }

    private void cleanVadBosTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeMessages(MSG_VAD_BOS);
        }
    }

    private void startVadBosTimer() {
        if (mTimerHandler != null) {
            Logger.d("VoiceService startVadBosTimer");
            mTimerHandler.removeMessages(MSG_VAD_BOS);
            mTimerHandler.sendEmptyMessageDelayed(MSG_VAD_BOS, mConf.getBosDuration());
        }
    }

    public synchronized void resumeAsr(boolean startBosNow) {
        mIsAsrPaused = false;
        if(startBosNow) {
            startVadBosTimer();
        } else {
            Logger.w("VoiceService BOS is NOT starting now !!");
        }
    }

    public synchronized void pauseAsr() {
        mIsAsrPaused = true;
        cleanVadBosTimer();
    }

    public void stop() {
        Logger.d("VoiceService stop");
        if (mWakeUpDetector != null) {
            mWakeUpDetector.close();
            mWakeUpDetector = null;
        }
        mVoiceRecorder.stop();
        mVoiceDetector.stopDetecting();

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onStopListening();
        }
        cleanVadBosTimer();
    }

    public void sleep() {
        if (mWakeUpDetector != null && mWakeUpDetector.isAwake()) {
            mWakeUpDetector.goSleep();
            if (mVoiceActiveStateListener != null) {
                mVoiceActiveStateListener.onSleep();
            }
        }
        cleanVadBosTimer();
    }

    public void wakeUp() {
        if (mWakeUpDetector != null && !mWakeUpDetector.isAwake()) {
            mWakeUpDetector.wakeUp();
            if (mVoiceActiveStateListener != null) {
                mVoiceActiveStateListener.onWakeUp();
            }
        }
        startVadBosTimer();
    }

    public void destroy() {
        Logger.d("VoiceService mWebService.release()");

        stop();

        if (mWebService == null) {
            return;
        }
        mWebService.release();
        mWebService = null;

        if (mMainThreadHandler != null) {
            mMainThreadHandler.removeCallbacksAndMessages(null);
            mMainThreadHandler = null;
        }
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
            mTimerHandler = null;
        }

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onDestroyed();
        }
    }

    public void sendCommand(String command, String alter) {
        if (mWebService != null) {
            mWebService.sendCommand(command, alter);
        }
    }

    public void updateAsrSettings(AsrConfiguration conf) {
        if (mWebService != null) {
            Logger.d("updateAsrSettings conf = " + conf.toJsonString());
            mWebService.sendCommand(SERVER_COMMAND_SETTINGS, conf.toJsonString());
            mConf.updateAsrConfiguration(conf);
        }
    }

    private WebSocket.OnWebSocketListener mWebSocketListener = new WebSocket.OnWebSocketListener() {
        @Override
        public void onOpen() {
            Logger.d("mWebSocketListener onOpen mMainThreadHandler = " + mMainThreadHandler);
            if (mMainThreadHandler != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mVoiceStateChangedListener != null) {
                            mVoiceStateChangedListener.onCreated();
                        }
                    }
                });
            }
        }

        @Override
        public void onMessage(final Message message) {
            cleanVadBosTimer();
            if (mMainThreadHandler != null) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mVoiceRecognitionListener != null) {
                            mVoiceRecognitionListener.onRecognitionResult(message);
                        }
                    }
                });
            }
        }

        @Override
        public void onWebSocketClosed() {
        }

        @Override
        public void onWebSocketError() {
        }
    };

    public void setVoiceRecognitionListener(VoiceRecognitionListener listener) {
        mVoiceRecognitionListener = listener;
    }

    public void setVoiceStateChangedListener(VoiceStateChangedListener listener) {
        mVoiceStateChangedListener = listener;
    }

    public void setVoiceActiveStateListener(VoiceActiveStateListener listener) {
        mVoiceActiveStateListener = listener;
    }

    private class VoiceDataSender implements IDataPath {

        @Override
        public void onData(byte[] data) {
            Logger.i("VoiceDataSender onData");
            if (mIsAsrPaused) {
                Logger.i("VoiceDataSender onData is pausing, drop data.");
                return;
            }
            if (mWebService != null) {
                mWebService.sendData(data);
            }
        }
    }

    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_VAD_BOS) {
                if (mVoiceStateChangedListener != null) {
                    mVoiceStateChangedListener.onVadBos();
                }
                return;
            } else if (msg.what == MSG_SEND_HEARTBEAT) {
                if (mWebService != null) {
                    mWebService.sendCommand("HEART-BEAT", "");
                }
                if (mTimerHandler != null) {
                    mTimerHandler.sendEmptyMessageDelayed(MSG_SEND_HEARTBEAT, HEARTBEAT_DURATION);
                }
                return;
            }
            super.handleMessage(msg);
        }
    }
}
