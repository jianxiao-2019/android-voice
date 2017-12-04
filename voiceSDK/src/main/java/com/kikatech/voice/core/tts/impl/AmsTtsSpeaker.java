package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.util.Pair;

import com.kikatech.voice.core.tts.TtsSpeaker;

/**
 * Created by ryanlin on 29/11/2017.
 */

public class AmsTtsSpeaker implements TtsSpeaker {
    @Override
    public void init(Context context, OnTtsInitListener listener) {

    }

    @Override
    public void close() {

    }

    @Override
    public void speak(String text) {

    }

    @Override
    public void speak(Pair<String, Integer>[] sentences) {

    }

    @Override
    public void interrupt() {

    }

    @Override
    public boolean isTtsSpeaking() {
        return false;
    }

    @Override
    public void setTtsStateChangedListener(TtsStateChangedListener listener) {

    }
}
