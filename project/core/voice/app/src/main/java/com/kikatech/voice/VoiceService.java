package com.kikatech.voice;

import android.content.Context;

import com.kikatech.voice.core.webservice.WebSocket;
import com.kikatech.voice.core.webservice.message.Message;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceService {

    private VoiceConfiguration mConf;
    private WebSocket mWebService;

    private VoiceService(VoiceConfiguration conf){
        mConf = conf;
        mWebService = WebSocket.openConnection(mWebSocketListener);
    }

    public static VoiceService getService(Context context, VoiceConfiguration conf){
        return new VoiceService(conf);
    }

    public void start(){
    }

    public void stop(){
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
}
