package com.kikatech.voice.core.webservice;

import com.kikatech.voice.core.webservice.impl.GoogleApi;

/**
 * Created by ryanlin on 2018/5/24.
 */

public class WebSocketUtil {

    public static IWebSocket openConnection(IWebSocket.OnWebSocketListener l) {
        return new GoogleApi(l);
    }
}
