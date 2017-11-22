package com.xiao.usbaudio;

import com.kikatech.usb.driver.impl.AudioBuffer;
import com.kikatech.voice.util.log.Logger;

public class AudioPlayBack {
    private static AudioBuffer sAudioBuffer;

    public static void write(byte[] decodedAudio, int len) {
        Logger.d("AudioPlayBack write size = " + decodedAudio.length + " len = " + len);
        sAudioBuffer.write(decodedAudio, decodedAudio.length);
    }

    public static void setup(AudioBuffer audioBuffer) {
        sAudioBuffer = audioBuffer;
    }

    public static void stop() {
        sAudioBuffer = null;
    }
}
