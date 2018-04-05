package com.kikatech.usb.driver.impl;

import android.support.annotation.NonNull;

import com.kikatech.usb.KikaBuffer;

/**
 * Created by ryanlin on 14/03/2018.
 */

public class KikaS2MBuff extends KikaBuffer {

    private final AudioBuffer mAudioBuffer;

    public KikaS2MBuff() {
        mAudioBuffer = new AudioBuffer(20000);
    }

    @Override
    public void onData(byte[] data, int len) {
        byte[] monoResult = new byte[len / 2];
        for (int i = 0; i < monoResult.length; i += 2) {
            monoResult[i] = data[i * 2];
            monoResult[i + 1] = data[i * 2 + 1];
        }
        mAudioBuffer.write(monoResult, monoResult.length);
    }

    @Override
    public void create() {

    }

    @Override
    public void close() {

    }

    @Override
    public void reset() {
        mAudioBuffer.clear();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mAudioBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }
}
