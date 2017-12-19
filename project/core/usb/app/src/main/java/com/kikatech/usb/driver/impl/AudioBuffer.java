package com.kikatech.usb.driver.impl;

import com.kikatech.voice.util.log.Logger;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tianli on 17-11-20.
 */

public class AudioBuffer {

    private final byte[] mBuffer;
    private int mBufferSize;

    private int mReadIndex = 0;
    private int mWriteIndex = 0;

    private ReentrantLock mLock = new ReentrantLock();

    public AudioBuffer(int size) {
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
            } else {
                int half = mBufferSize - mWriteIndex;
                System.arraycopy(audioData, 0, mBuffer, mWriteIndex, half);
                System.arraycopy(audioData, half, mBuffer, 0, sizeInBytes - half);
                if (mReadIndex < (sizeInBytes - half)) {
                    Logger.w("Some data was been overridden.");
                    mReadIndex = sizeInBytes - half;
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
}
