package com.kikatech.voice.core.tts;

import android.util.Pair;

/**
 * Created by tianli on 17-10-31.
 */

public interface TtsSpeaker {

    int SUPPORTED_VOICE_COUNT = 2;
    int TTS_VOICE_1 = 0;
    int TTS_VOICE_2 = 1;

    interface TtsStateChangedListener {
        void onTtsStart();

        void onTtsComplete();

        void onTtsInterrupted();

        void onTtsError();
    }

    void speak(String text);

    void speak(Pair<String, Integer>[] sentences);

    void interrupt();

    void setTtsStateChangedListener(TtsStateChangedListener listener);
}
