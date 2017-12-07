package com.kikatech.voice.core.tts;

import android.content.Context;
import android.util.Pair;

/**
 * Created by tianli on 17-10-31.
 */

public interface TtsSource {

    int SUPPORTED_VOICE_COUNT = 2;
    int TTS_SPEAKER_1 = 0;
    int TTS_SPEAKER_2 = 1;

    int INIT_SUCCESS = 0;
    int INIT_FAIL = 1;

    interface TtsStateChangedListener {
        void onTtsStart();

        void onTtsComplete();

        void onTtsInterrupted();

        void onTtsError();
    }

    interface OnTtsInitListener {
        void onTtsInit(int state);
    }

    void init(Context context, OnTtsInitListener listener);

    void close();

    void speak(String text);

    void speak(Pair<String, Integer>[] sentences);

    void interrupt();

    boolean isTtsSpeaking();

    void setTtsStateChangedListener(TtsStateChangedListener listener);
}
