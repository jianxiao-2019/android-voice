package com.kikatech.voice;

import android.content.Context;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.recorder.VoiceRecorder;
import com.kikatech.voice.core.recorder.VoiceSource;
import com.kikatech.voice.core.vad.VoiceDetector;
import com.kikatech.voice.core.webservice.WebSocket;
import com.kikatech.voice.core.webservice.message.Message;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceService {

    private VoiceConfiguration mConf;
    private WebSocket mWebService;

    private VoiceRecorder mVoiceRecorder;
    private VoiceDetector mVoiceDetector;

    private VoiceService(VoiceConfiguration conf) {
        mConf = conf;
        mWebService = WebSocket.openConnection(mWebSocketListener);

        // TODO : base on the VoiceConfiguration.
        mVoiceDetector = new VoiceDetector(new FileWriter(mConf.getDebugFilePath() + "_speex", new VoiceDataSender()));
        mVoiceRecorder = new VoiceRecorder(new VoiceSource(), new FileWriter(mConf.getDebugFilePath(), mVoiceDetector));
    }

    public static VoiceService getService(Context context, VoiceConfiguration conf) {
        return new VoiceService(conf);
    }

    public void start() {
        mVoiceDetector.startDetecting();
        mVoiceRecorder.start();
    }

    public void stop() {
        mVoiceRecorder.stop();
        mVoiceDetector.stopDetecting();
    }

    private WebSocket.OnWebSocketListener mWebSocketListener = new WebSocket.OnWebSocketListener() {
        @Override
        public void onMessage(Message message) {
        }

        @Override
        public void onWebSocketClosed() {
        }

        @Override
        public void onWebSocketError() {
        }
    };

    private class VoiceDataSender implements IDataPath {

        @Override
        public void onData(byte[] data) {

        }
    }
}
