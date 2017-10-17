package com.kikatech.vkb.engine.recorder;

/**
 * Created by ryanlin on 06/10/2017.
 */

public interface VoiceDetectorListener {
    void onRecorded(byte[] data);
    void onSpeechProbabilityChanged(float speechProbability);
}
