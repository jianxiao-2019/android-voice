package com.kikatech.voice;

import android.content.Context;
import android.os.Handler;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.recorder.VoiceRecorder;
import com.kikatech.voice.core.recorder.VoiceSource;
import com.kikatech.voice.core.vad.VoiceDetector;
import com.kikatech.voice.core.webservice.WebSocket;
import com.kikatech.voice.core.webservice.message.Message;
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

    private VoiceService(VoiceConfiguration conf) {
        mConf = conf;

        // TODO : base on the VoiceConfiguration.
        mVoiceDetector = new VoiceDetector(new FileWriter(mConf.getDebugFilePath() + "_speex", new VoiceDataSender()));
        mVoiceRecorder = new VoiceRecorder(new VoiceSource(), new FileWriter(mConf.getDebugFilePath(), mVoiceDetector));
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

//        Message.register();
    }

    public void stop() {
        Logger.d("VoiceService stop");
        mVoiceRecorder.stop();
        mVoiceDetector.stopDetecting();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.d("VoiceService mWebService.release()");
                mWebService.release();
                mWebService = null;
            }
        }, WEBSOCKET_CLOSE_DELAY);
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
            Logger.d("VoiceDataSender onData");
            if (mWebService != null) {
                mWebService.sendData(data);
            }
        }
    }
}
