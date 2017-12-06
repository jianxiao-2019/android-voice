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
import com.kikatech.voice.core.webservice.message.ConfigMessage;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceService implements WakeUpDetector.OnHotWordDetectListener {

    public static final int REASON_NOT_CREATED = 1;

    private VoiceConfiguration mConf;
    private WebSocket mWebService;

    private VoiceRecorder mVoiceRecorder;
    private VoiceDetector mVoiceDetector;
    private NoiseSuppression mNoiseSuppression;
    private WakeUpDetector mWakeUpDetector;

    private VoiceRecognitionListener mVoiceRecognitionListener;
    private VoiceStateChangedListener mVoiceStateChangedListener;
    private VoiceActiveStateListener mVoiceActiveStateListener;

    private Handler mMainThreadHander;

    private boolean mIsAsrPaused = false;

    @Override
    public void onDetected() {
        if (mVoiceActiveStateListener != null) {
            mVoiceActiveStateListener.onWakeUp();
        }
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
    }

    public interface VoiceActiveStateListener {
        void onWakeUp();

        void onSleep();
    }

    private VoiceService(VoiceConfiguration conf) {
        mConf = conf;

        IVoiceSource voiceSource = mConf.getVoiceSource();
        if (voiceSource == null) {
            voiceSource = new VoiceSource();
        }


        // TODO : base on the VoiceConfiguration.
        mVoiceDetector = new VoiceDetector(
                new FileWriter(mConf.getDebugFilePath() + "_speex", new VoiceDataSender()), new VoiceDetector.OnVadProbabilityChangeListener() {
            @Override
            public void onSpeechProbabilityChanged(final float speechProbability) {
                if (mMainThreadHander != null) {
                    mMainThreadHander.post(new Runnable() {
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

        mNoiseSuppression = new NoiseSuppression(new FileWriter(mConf.getDebugFilePath() + "_NS", mVoiceDetector));

        Logger.d("mConf.isSupportWakeUpMode() = " + mConf.isSupportWakeUpMode());
        if (mConf.isSupportWakeUpMode()) {
            mWakeUpDetector = WakeUpDetector.getDetector(this, mNoiseSuppression);
            mVoiceRecorder = new VoiceRecorder(voiceSource, new FileWriter(mConf.getDebugFilePath(), mWakeUpDetector));
        } else {
            mVoiceRecorder = new VoiceRecorder(voiceSource, new FileWriter(mConf.getDebugFilePath(), mNoiseSuppression));
        }

        Message.register("CONFIG", ConfigMessage.class);
    }

    public static VoiceService getService(Context context, VoiceConfiguration conf) {
        return new VoiceService(conf);
    }

    public void create() {
        if (mWebService != null) {
            mWebService.release();
        }

        mMainThreadHander = new Handler();

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
        mVoiceDetector.startDetecting();
        mVoiceRecorder.start();

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onStartListening();
        }
    }

    public void resumeAsr() {
        mIsAsrPaused = false;
    }

    public void pauseAsr() {
        mIsAsrPaused = true;
    }

    public void stop() {
        Logger.d("VoiceService stop");
        mVoiceRecorder.stop();
        mVoiceDetector.stopDetecting();

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onStopListening();
        }
    }

    public void sleep() {
        if (mWakeUpDetector != null) {
            mWakeUpDetector.goSleep();
        }
        if (mVoiceActiveStateListener != null) {
            mVoiceActiveStateListener.onSleep();
        }
    }

    public void destroy() {
        Logger.d("VoiceService mWebService.release()");
        if (mWebService == null) {
            return;
        }
        mWebService.release();
        mWebService = null;

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onDestroyed();
        }
    }

    public void sendCommand(String command, String alter) {
        if (mWebService != null) {
            mWebService.sendCommand(command, alter);
        }
    }

    private WebSocket.OnWebSocketListener mWebSocketListener = new WebSocket.OnWebSocketListener() {
        @Override
        public void onOpen() {
            Logger.d("mWebSocketListener onOpen mMainThreadHander = " + mMainThreadHander);
            if (mMainThreadHander != null) {
                mMainThreadHander.post(new Runnable() {
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
            if (message instanceof ConfigMessage) {
                mConf.updateServerConfig((ConfigMessage) message);
                mVoiceDetector.updatePacketInterval(((ConfigMessage) message).packetInterval);
                return;
            }
            if (mMainThreadHander != null) {
                mMainThreadHander.post(new Runnable() {
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
}
