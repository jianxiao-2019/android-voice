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
        mAudioDriver = driver;
        mAudioDriver.setOnDataListener(this);
        mKikaNcBuffer = new KikaNcBuffer();
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
            Logger.w("Don't call start() after device detached.");
        }
    }

    @Override
    public void stop() {
        if (mAudioDriver != null) {
            mAudioDriver.stopRecording();
            mKikaNcBuffer.stop();
        } else {
            Logger.w("Don't call stop() after device detached.");
        }
    }

    @Override
    public void close() {
        mKikaNcBuffer.close();
    }

    public void closeDevice() {
        if (mAudioDriver != null) {
            mAudioDriver.close();
            mAudioDriver = null;
        }
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (mAudioDriver == null) {
            Logger.w("Don't call read() after device detached.");
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

    public int getNoiseSuppressionParameters(int mode) {
        return mKikaNcBuffer.getNoiseSuppressionParameters(mode);
    }

    @Override
    public void onData(byte[] data, int length) {
        mKikaNcBuffer.onData(data, length);
    }

    public int checkVolumeState() {
        if (mAudioDriver != null) {
            return mAudioDriver.checkVolumeState();
        }
        return -1;
    }

    public int volumeUp() {
        if (mAudioDriver != null) {
            return mAudioDriver.volumeUp();
        }
        return -1;
    }

    public int volumeDown() {
        if (mAudioDriver != null) {
            return mAudioDriver.volumeDown();
        }
        return -1;
    }
}
