package com.kikatech.usb.driver.impl;

import android.support.annotation.NonNull;

import com.kikatech.usb.KikaBuffer;

/**
 * Created by ryanlin on 02/04/2018.
 */

public class KikaSimpleBuff extends KikaBuffer {

    private final AudioBuffer mAudioBuffer;

    public KikaSimpleBuff() {
        mAudioBuffer = new AudioBuffer(20000);
    }

    @Override
    public void onData(byte[] data, int len) {
        mAudioBuffer.write(data, len);
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