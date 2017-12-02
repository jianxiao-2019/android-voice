package com.kikatech.usb.driver.impl;

import com.kikatech.voice.util.log.Logger;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tianli on 17-11-20.
 */

public class AudioBuffer {

    private final byte[] mBuffer;
    private int mBufferSize;

    private int mOffset = 0;
    private int mLength = 0;

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
            if (sizeInBytes + mLength < mBufferSize) {
                System.arraycopy(audioData, 0, mBuffer, mLength, sizeInBytes);
            } else {
                int half = mBufferSize - mLength;
                System.arraycopy(audioData, 0, mBuffer, sizeInBytes, half);
                System.arraycopy(audioData, half, mBuffer, 0, sizeInBytes - half);
                if (mOffset < (sizeInBytes - half)) {
                    Logger.w("Some data was been overridden.");
                    mOffset = sizeInBytes - half;
                }
            }
            mLength = (mLength + sizeInBytes) % mBufferSize;
        } finally {
            mLock.unlock();
        }
    }

    public int read(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        try {
            mLock.lock();
            int curLength = mLength;
            if (mLength < mOffset) {
                curLength += mBufferSize;
            }
            int size = Math.min(sizeInBytes, (curLength - mOffset));
            if (size > 0) {
                if (size <= mBufferSize - mOffset) {
                    System.arraycopy(mBuffer, mOffset, audioData, offsetInBytes, size);
                } else {
                    int half = mBufferSize - mOffset;
                    System.arraycopy(mBuffer, mOffset, audioData, offsetInBytes, half);
                    System.arraycopy(mBuffer, 0, audioData, offsetInBytes + half, size - half);
                }
                mOffset = (mOffset + size) % mBufferSize;
                return size;
            }
            return 0;
        } finally {
            mLock.unlock();
        }
    }
}
