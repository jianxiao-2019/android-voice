package com.kikago.speech.baidu;

import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tianli on 17-11-20.
 */

public class CircularBuffer {
    private static final String TAG = "CircularBuffer";

    private final byte[] mBuffer;
    private int mBufferSize;

    private int mReadIndex = 0;
    private int mWriteIndex = 0;

    private ReentrantLock mLock = new ReentrantLock();

    public CircularBuffer(int size) {
        mBufferSize = size;
        mBuffer = new byte[mBufferSize];
    }

    public void write(byte[] audioData, int sizeInBytes) {
        try {
            mLock.lock();
            if (sizeInBytes > mBufferSize) {
                throw new IllegalArgumentException("sizeInBytes exceed buffer size");
            }
            if (mWriteIndex + sizeInBytes < mBufferSize) {
                System.arraycopy(audioData, 0, mBuffer, mWriteIndex, sizeInBytes);
                if (mWriteIndex < mReadIndex && mReadIndex < mWriteIndex + sizeInBytes) {
                    Log.w(TAG, "Some data was been overflowed. 1");
                    mReadIndex = mWriteIndex + sizeInBytes + 1;
                }
            } else {
                int half = mBufferSize - mWriteIndex;
                System.arraycopy(audioData, 0, mBuffer, mWriteIndex, half);
                System.arraycopy(audioData, half, mBuffer, 0, sizeInBytes - half);
                if (mWriteIndex < mReadIndex) {
                    Log.w(TAG, "Some data was been overflowed. 2");
                    mReadIndex = mWriteIndex + 1;
                } else if (mReadIndex < (sizeInBytes - half)) {
                    Log.w(TAG, "Some data was been overflowed. 3");
                    mReadIndex = sizeInBytes - half + 1;
                }
            }
            mWriteIndex = (mWriteIndex + sizeInBytes) % mBufferSize;
        } finally {
            mLock.unlock();
        }
    }

    public int read(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        try {
            mLock.lock();
            int curWIndex = mWriteIndex;
            if (mWriteIndex < mReadIndex) {
                curWIndex += mBufferSize;
            }
            int size = Math.min(sizeInBytes, (curWIndex - mReadIndex));
            if (size > 0) {
                if (size <= mBufferSize - mReadIndex) {
                    System.arraycopy(mBuffer, mReadIndex, audioData, offsetInBytes, size);
                } else {
                    int half = mBufferSize - mReadIndex;
                    System.arraycopy(mBuffer, mReadIndex, audioData, offsetInBytes, half);
                    System.arraycopy(mBuffer, 0, audioData, offsetInBytes + half, size - half);
                }
                mReadIndex = (mReadIndex + size) % mBufferSize;
                return size;
            }
            return 0;
        } finally {
            mLock.unlock();
        }
    }

    public void clear() {
        try {
            mLock.lock();
            mReadIndex = 0;
            mWriteIndex = 0;
        } finally {
            mLock.unlock();
        }
    }

    public boolean hasData() {
        try {
            mLock.lock();
            return mWriteIndex != mReadIndex;
        } finally {
            mLock.unlock();
        }
    }
}
