package com.kikago.speech.baidu;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class ForBaiduInputStream extends InputStream {

    private static final String TAG = "ForBaiduInputStream";

    private static final int BUFFER_SIZE = 200000;

    private CircularBuffer mCircularBuffer;

    public void start() {
        mCircularBuffer = new CircularBuffer(BUFFER_SIZE);
    }

    public void writeByte(byte[] audioData, int sizeInBytes) {
        mCircularBuffer.write(audioData, sizeInBytes);
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    private long mLastTime = -1;
    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        int size = mCircularBuffer.read(b, off, len);
        if (System.currentTimeMillis() - mLastTime > 2000L) {
            Log.d(TAG, "read size = " + size);
            mLastTime = System.currentTimeMillis();
        }
        return size;
    }

    @Override
    public void close() throws IOException {
        mCircularBuffer.clear();
    }

    public boolean hasData() {
        return mCircularBuffer != null && mCircularBuffer.hasData();
    }
}
