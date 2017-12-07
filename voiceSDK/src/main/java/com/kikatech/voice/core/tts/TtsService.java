package com.kikatech.voice.core.tts;

import com.kikatech.voice.core.tts.impl.AndroidTtsSource;
import com.kikatech.voice.core.tts.impl.KikaTtsSource;

/**
 * Created by tianli on 17-10-28.
 */

public class TtsService {

    public enum TtsSourceType {
        ANDROID,
        KIKA_WEB,
    }

    private static TtsService sTtsService;
    public static TtsService getInstance() {
        if (sTtsService == null) {
            sTtsService = new TtsService();
        }
        return sTtsService;
    }

    private TtsService () {

    }

    public TtsSource getSpeaker(TtsSourceType type){
        if (type == TtsSourceType.ANDROID) {
            return new AndroidTtsSource();
        }
        return new KikaTtsSource();
    }
}
