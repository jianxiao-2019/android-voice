package com.kikatech.voice;

import android.content.Context;

import com.kikatech.voice.core.webservice.WebSocket;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceService {

    private VoiceConfiguration mConf;
    private WebSocket mWebService;

    private VoiceService(VoiceConfiguration conf){
        mConf = conf;
        mWebService = WebSocket.openConnection();
    }

    public static VoiceService getService(Context context, VoiceConfiguration conf){
        return new VoiceService(conf);
    }

    public void start(){
    }

    public void stop(){
    }
}
