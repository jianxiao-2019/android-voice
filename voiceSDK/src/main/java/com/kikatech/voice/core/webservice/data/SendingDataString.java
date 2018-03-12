package com.kikatech.voice.core.webservice.data;

import org.java_websocket.client.WebSocketClient;

/**
 * Created by ryanlin on 13/03/2018.
 */

public class SendingDataString extends SendingData {

    private String mStr;

    public SendingDataString(String str) {
        this.mStr = str;
    }

    @Override
    public boolean send(WebSocketClient client) {
        try {
            client.send(mStr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
