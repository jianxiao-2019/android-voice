package com.kikatech.voice.core.webservice;

import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.conf.VoiceConfiguration;

/**
 * Created by ryanlin on 2018/5/24.
 */

public interface IWebSocket {

    @interface WebSocketError {
        int WEB_SOCKET_CLOSED = 0;
        int DATA_ERROR = 1;
        int EMPTY_RESULT = 2;
    }

    interface OnWebSocketListener {
        void onMessage(Message message);

        void onError(@WebSocketError int errorCode);
    }

    void connect(VoiceConfiguration voiceConfiguration);

    void release();

    void onStart();

    void onStop();

    void sendCommand(final String command, final String payload);

    void sendData(byte[] data);

    boolean isConnected();
}
