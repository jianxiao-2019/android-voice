package com.kikatech.voice.core.webservice.data;

import org.java_websocket.client.WebSocketClient;

/**
 * Created by ryanlin on 13/03/2018.
 */

public class SendingDataByte extends SendingData {

    private byte[] mData;

    public SendingDataByte(byte[] data) {
        this.mData = data;
    }

    @Override
    public boolean send(WebSocketClient client) {
        try {
            client.send(mData);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
