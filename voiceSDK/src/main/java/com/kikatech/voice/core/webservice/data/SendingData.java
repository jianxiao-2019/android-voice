package com.kikatech.voice.core.webservice.data;

import org.java_websocket.client.WebSocketClient;

/**
 * Created by ryanlin on 13/03/2018.
 */

public abstract class SendingData {
    public abstract boolean send(WebSocketClient client);
}
