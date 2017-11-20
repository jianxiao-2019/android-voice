package com.kikatech.usb.driver.impl;

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
            int writeOffset = (mOffset + mLength) % mBufferSize;
            if (sizeInBytes <= mBufferSize - writeOffset) {
                System.arraycopy(audioData, 0, mBuffer, writeOffset, sizeInBytes);
                mLength += sizeInBytes;
            } else {
                int half = mBufferSize - writeOffset;
                // 拷贝到buffer的后半部分
                System.arraycopy(audioData, 0, mBuffer, writeOffset, half);
                // 拷贝到buffer的前半部分
                System.arraycopy(audioData, half, mBuffer, 0, sizeInBytes - half);
                mLength += sizeInBytes;
                if (sizeInBytes - half > mOffset) {
                    mLength -= sizeInBytes - half - mOffset;
                    // 部分未读数据被新数据覆盖，mOffset需要后移
                    mOffset = sizeInBytes - half;
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    public int read(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        try {
            mLock.lock();
            int size = Math.min(sizeInBytes, mLength);
            if (size > 0) {
                if (size <= mBufferSize - mOffset) {
                    System.arraycopy(mBuffer, mOffset, audioData, offsetInBytes, size);
                }else {
                    int half = mBufferSize - mOffset;
                    System.arraycopy(mBuffer, mOffset, audioData, offsetInBytes, half);
                    System.arraycopy(mBuffer, 0, audioData, offsetInBytes + half, size - half);
                }
                mLength -= size;
                return size;
            }
            return 0;
        } finally {
            mLock.unlock();
        }
    }
}
