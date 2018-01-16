package com.kikatech.usb;

import android.support.annotation.NonNull;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.usb.nc.KikaNcBuffer;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by tianli on 17-11-6.
 * Update by ryanlin on 25/12/2017.
 */

public class UsbAudioSource implements IVoiceSource, UsbAudioDriver.OnDataListener {

    public static final int READ_FAIL = -99;

    private UsbAudioDriver mAudioDriver;
    private KikaNcBuffer mKikaNcBuffer;

    public UsbAudioSource(UsbAudioDriver driver) {
        mKikaNcBuffer = new KikaNcBuffer();
        mAudioDriver = driver;
        mAudioDriver.setOnDataListener(this);
    }

    @Override
    public void open() {
        mKikaNcBuffer.create();
    }

    @Override
    public void start() {
        if (mAudioDriver != null) {
            mAudioDriver.startRecording();
            mKikaNcBuffer.start();
        } else {
            Logger.w("Don't call start() after close().");
        }
    }

    @Override
    public void stop() {
        if (mAudioDriver != null) {
            mAudioDriver.stopRecording();
            mKikaNcBuffer.stop();
        } else {
            Logger.w("Don't call stop() after close().");
        }
    }

    @Override
    public void close() {
        if (mAudioDriver != null) {
            mAudioDriver.close();
            mAudioDriver = null;
            mKikaNcBuffer.close();
        }
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (mAudioDriver == null) {
            Logger.w("Don't call read() after close().");
            return READ_FAIL;
        }
        return mKikaNcBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public int getBufferSize() {
        return KikaNcBuffer.BUFFER_SIZE;
    }

    public void setNoiseCancellationParameters(int mode, int value) {
        mKikaNcBuffer.setNoiseSuppressionParameters(mode, value);
    }

    @Override
    public void onData(byte[] data, int length) {
        mKikaNcBuffer.onData(data, length);
    }
}
