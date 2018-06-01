package com.kikatech.usb.nc;

import android.support.annotation.NonNull;

import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.buffer.CircularBuffer;
import com.kikatech.usb.util.DataUtil;
import com.kikatech.voice.util.log.Logger;

import lib.android.anc.NoiseCancellation;

/**
 * Created by ryanlin on 15/01/2018.
 */

public class KikaNcBuffer extends KikaBuffer {

    public static final int CONTROL_ANGLE = 0;
    public static final int CONTROL_NC = 1;
    public static final int CONTROL_MODE = 2;

    private final CircularBuffer mCircularBuffer;
    private final byte[] mAudioBytes = new byte[BUFFER_SIZE];
    private int mOffset = 0;

    public KikaNcBuffer() {
        mCircularBuffer = new CircularBuffer(20000);
    }

    @Override
    public void onData(byte[] data, int len) {
        int tempLen = len;
        int tempIdx = 0;
        int length;
        while (tempLen + mOffset >= BUFFER_SIZE) {
            length = BUFFER_SIZE - mOffset;
            System.arraycopy(data, tempIdx, mAudioBytes, mOffset, length);
            tempLen -= length;
            tempIdx += length;
            mCircularBuffer.write(doNoiseCancellation(), mAudioBytes.length / 2);
            mOffset = 0;
        }
        System.arraycopy(data, tempIdx, mAudioBytes, mOffset, tempLen);
        mOffset += tempLen;
    }

    private byte[] doNoiseCancellation() {
        short[] outBuffs = new short[mAudioBytes.length / 2];
        NoiseCancellation.NoiseMask(DataUtil.byteToShort(mAudioBytes), outBuffs);

        return DataUtil.shortToByte(outBuffs);
    }

    @Override
    public void create() {
        Logger.e("KikaNcBuffer create");
        NoiseCancellation.Init();
    }

    @Override
    public void close() {
        Logger.e("KikaNcBuffer close");
        NoiseCancellation.Destroy();
    }

    @Override
    public void reset() {
        mCircularBuffer.clear();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mCircularBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    public static void setNoiseSuppressionParameters(int mode, int value) {
        NoiseCancellation.SetControl(mode, value);
    }

    public static int getNoiseSuppressionParameters(int mode) {
        return NoiseCancellation.GetControl(mode);
    }

    public static int getVersion() {
        return NoiseCancellation.GetVersion();
    }

}
