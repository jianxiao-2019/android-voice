package com.kikatech.voice.core.tts;

import com.kikatech.voice.core.tts.impl.AndroidTtsSpeaker;

/**
 * Created by tianli on 17-10-28.
 */

public class TtsService {

    private static TtsService sTtsService;
    public static TtsService getInstance() {
        if (sTtsService == null) {
            sTtsService = new TtsService();
        }
        return sTtsService;
    }

    private TtsService () {

    }

    public TtsSpeaker getSpeaker(){
        return new AndroidTtsSpeaker();
    }
}
