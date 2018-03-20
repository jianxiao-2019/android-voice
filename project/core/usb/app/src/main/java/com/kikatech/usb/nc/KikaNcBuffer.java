package com.kikatech.usb.nc;

import android.support.annotation.NonNull;

import com.kikatech.usb.KikaBuffer;
import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.usb.driver.impl.AudioBuffer;
import com.kikatech.voice.util.log.Logger;

import lib.android.anc.NoiseCancellation;

/**
 * Created by ryanlin on 15/01/2018.
 */

public class KikaNcBuffer extends KikaBuffer {

    public static final int BUFFER_SIZE = 512;

    public static final int CONTROL_ANGLE = 0;
    public static final int CONTROL_NC = 1;
    public static final int CONTROL_MODE = 2;

    private final AudioBuffer mAudioBuffer;
    private final byte[] mAudioBytes = new byte[BUFFER_SIZE];
    private int mOffset = 0;

    public KikaNcBuffer() {
        mAudioBuffer = new AudioBuffer(20000);
    }

    @Override
    public void onData(byte[] data, int len) {
        Logger.d("778893 KikaNcBuffer onData len = " + len);
        int tempLen = len;
        int tempIdx = 0;
        int length;
        while (tempLen + mOffset >= BUFFER_SIZE) {
            length = BUFFER_SIZE - mOffset;
            System.arraycopy(data, tempIdx, mAudioBytes, mOffset, length);
            tempLen -= length;
            tempIdx += length;
            mAudioBuffer.write(doNoiseCancellation(), mAudioBytes.length / 2);
            mOffset = 0;
        }
        System.arraycopy(data, tempIdx, mAudioBytes, mOffset, tempLen);
        mOffset += tempLen;
        Logger.d("778893 KikaNcBuffer onData final mOffset = " + mOffset);
    }

    private byte[] doNoiseCancellation() {
        Logger.d("778893 KikaNcBuffer doNoiseCancellation");
        short[] outBuffs = new short[mAudioBytes.length / 2];
        NoiseCancellation.NoiseMask(ByteToShort(mAudioBytes), outBuffs);

        return ShortToByte(outBuffs);
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
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mAudioBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    private short[] ByteToShort(byte[] bytes) {
        int len = bytes.length / 2;
        short[] shorts = new short[len];
        for (int i = 0; i < len; ++i) {
            shorts[i] = (short) ((bytes[i * 2 + 1] << 8) | (bytes[i * 2] & 0xff));
        }
        return shorts;
    }

    private byte[] ShortToByte(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        for (int i = 0; i < shorts.length; i++) {
            bytes[2 * i] = (byte) (shorts[i] & 0xff);
            bytes[2 * i + 1] = (byte) ((shorts[i] >> 8) & 0xff);
        }

        return bytes;
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
