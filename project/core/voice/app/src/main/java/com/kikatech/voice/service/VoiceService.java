package com.kikatech.voice.service;

import android.content.Context;
import android.os.Handler;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.recorder.VoiceRecorder;
import com.kikatech.voice.core.recorder.VoiceSource;
import com.kikatech.voice.core.vad.VoiceDetector;
import com.kikatech.voice.core.webservice.WebSocket;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceService {
    
    private static final long WEBSOCKET_CLOSE_DELAY = 2000;

    private VoiceConfiguration mConf;
    private WebSocket mWebService;

    private VoiceRecorder mVoiceRecorder;
    private VoiceDetector mVoiceDetector;

    private VoiceRecognitionListener mVoiceRecognitionListener;
    private VoiceStateChangedListener mVoiceStateChangedListener;

    private Handler mMainHandler;

    public interface VoiceRecognitionListener {
        void onRecognitionResult(Message message);
    }

    public interface VoiceStateChangedListener {
        void onStartListening();
        void onStopListening();
        void onSpeechProbabilityChanged(float prob);
    }

    private VoiceService(VoiceConfiguration conf) {
        mConf = conf;
        // TODO : base on the VoiceConfiguration.
        mVoiceDetector = new VoiceDetector(
                new FileWriter(mConf.getDebugFilePath() + "_speex", new VoiceDataSender()), new VoiceDetector.OnVadProbabilityChangeListener() {
            @Override
            public void onSpeechProbabilityChanged(final float speechProbability) {
                if (mMainHandler != null) {
                    mMainHandler.post(new Runnable() {
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

        IVoiceSource voiceSource = conf.getVoiceSource();
        Logger.i("VoiceService voiceSource = " + voiceSource);
        if (voiceSource == null) {
            voiceSource = new VoiceSource();
        }
        mVoiceRecorder = new VoiceRecorder(voiceSource, new FileWriter(mConf.getDebugFilePath(), mVoiceDetector));
    }

    public static VoiceService getService(Context context, VoiceConfiguration conf) {
        return new VoiceService(conf);
    }

    public void start() {
        Logger.d("VoiceService start");
        mVoiceDetector.startDetecting();
        mVoiceRecorder.start();
        if (mWebService != null) {
            mWebService.release();
        }
        mWebService = WebSocket.openConnection(mWebSocketListener);
        mWebService.connect(mConf.getConnectionConfiguration());

        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onStartListening();
        }

        mMainHandler = new Handler();
    }

    public void stop() {
        Logger.d("VoiceService stop");
        mVoiceRecorder.stop();
        mVoiceDetector.stopDetecting();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.d("VoiceService mWebService.release()");
                if (mWebService == null) {
                    return;
                }
                mWebService.release();
                mWebService = null;
            }
        }, WEBSOCKET_CLOSE_DELAY);
        if (mVoiceStateChangedListener != null) {
            mVoiceStateChangedListener.onStopListening();
        }
    }

    public void sendCommand(String command, String alter) {
        if (mWebService != null) {
            mWebService.sendCommand(command, alter);
        }
    }

    private WebSocket.OnWebSocketListener mWebSocketListener = new WebSocket.OnWebSocketListener() {
        @Override
        public void onMessage(final Message message) {
            if (mMainHandler != null) {
                mMainHandler.post(new Runnable() {
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

    private class VoiceDataSender implements IDataPath {

        @Override
        public void onData(byte[] data) {
            Logger.i("VoiceDataSender onData");
            if (mWebService != null) {
                mWebService.sendData(data);
            }
        }
    }
}
