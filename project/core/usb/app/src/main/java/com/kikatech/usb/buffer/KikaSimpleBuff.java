package com.kikatech.usb.buffer;

import android.support.annotation.NonNull;

/**
 * Created by ryanlin on 02/04/2018.
 */

public class KikaSimpleBuff extends KikaBuffer {

    private final CircularBuffer mCircularBuffer;

    public KikaSimpleBuff() {
        mCircularBuffer = new CircularBuffer(20000);
    }

    @Override
    public void onData(byte[] data, int len) {
        mCircularBuffer.write(data, len);
    }

    @Override
    public void create() {

    }

    @Override
    public void close() {

    }

    @Override
    public void reset() {
        mCircularBuffer.clear();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mCircularBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }
}