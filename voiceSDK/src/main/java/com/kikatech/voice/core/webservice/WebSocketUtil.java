package com.kikatech.voice.core.webservice;

import com.kikatech.voice.core.webservice.impl.WebSocket;

/**
 * Created by ryanlin on 2018/5/24.
 */

public class WebSocketUtil {

    public static IWebSocket openConnection(WebSocket.OnWebSocketListener l) {
        return new WebSocket(l);
    }
}
