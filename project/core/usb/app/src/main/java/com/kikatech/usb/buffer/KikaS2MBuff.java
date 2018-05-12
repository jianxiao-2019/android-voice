package com.kikatech.usb.buffer;

import android.support.annotation.NonNull;

/**
 * Created by ryanlin on 14/03/2018.
 */

public class KikaS2MBuff extends KikaBuffer {

    private final CircularBuffer mCircularBuffer;

    public KikaS2MBuff() {
        mCircularBuffer = new CircularBuffer(20000);
    }

    @Override
    public void onData(byte[] data, int len) {
        // TODO : Use the DataUtil
        byte[] monoResult = new byte[len / 2];
        for (int i = 0; i < monoResult.length; i += 2) {
            monoResult[i] = data[i * 2];
            monoResult[i + 1] = data[i * 2 + 1];
        }
        mCircularBuffer.write(monoResult, monoResult.length);
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
