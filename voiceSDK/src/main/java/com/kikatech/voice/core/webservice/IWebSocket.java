package com.kikatech.voice.core.webservice;

import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.service.conf.VoiceConfiguration;

/**
 * Created by ryanlin on 2018/5/24.
 */

public interface IWebSocket {

    interface OnWebSocketListener {
        void onMessage(Message message);

        void onWebSocketClosed();

        void onWebSocketError();
    }

    void connect(VoiceConfiguration voiceConfiguration);

    void release();

    void startListening();

    void stopListening();

    void sendCommand(final String command, final String payload);

    void sendData(byte[] data);

    boolean isConnected();
}
