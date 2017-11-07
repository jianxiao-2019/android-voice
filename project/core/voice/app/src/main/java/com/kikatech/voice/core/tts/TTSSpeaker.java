package com.kikatech.voice.core.tts;

/**
 * Created by tianli on 17-10-31.
 */

public interface TtsSpeaker {

    interface TtsStateChangedListener {
        void onTtsStart();
        void onTtsComplete();
        void onTtsInterrupted();
        void onTtsError();
    }

    void speak(String text);
    void interrupt();
    void setTtsStateChangedListener(TtsStateChangedListener listener);
}
