package com.kikatech.usb;

import android.support.annotation.NonNull;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.usb.driver.impl.KikaS2MBuff;
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
    private KikaBuffer mKikaBuffer;

    private boolean mIsOpened = false;

    private SourceDataCallback mSourceDataCallback;

    public interface SourceDataCallback {
        void onSource(byte[] leftData, byte[] rightData);
    }

    public UsbAudioSource(UsbAudioDriver driver) {
        mAudioDriver = driver;
        mAudioDriver.setOnDataListener(this);
        mKikaBuffer = KikaBuffer.getKikaBuffer(KikaBuffer.TYPE_NOISC_CANCELLATION);
    }

    @Override
    public void open() {
        mIsOpened = true;
        mKikaBuffer.create();
    }

    @Override
    public void start() {
        if (mAudioDriver != null) {
            mAudioDriver.startRecording();
        } else {
            Logger.w("Don't call start() after device detached.");
        }
    }

    @Override
    public void stop() {
        if (mAudioDriver != null) {
            mAudioDriver.stopRecording();
        } else {
            Logger.w("Don't call stop() after device detached.");
        }
    }

    @Override
    public void close() {
        mKikaBuffer.close();
        mIsOpened = false;
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
        return mKikaBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public int getBufferSize() {
        return KikaNcBuffer.BUFFER_SIZE;
    }

    public void setNoiseCancellationParameters(int mode, int value) {
        KikaNcBuffer.setNoiseSuppressionParameters(mode, value);
    }

    public int getNoiseSuppressionParameters(int mode) {
        return KikaNcBuffer.getNoiseSuppressionParameters(mode);
    }

    @Override
    public void onData(byte[] data, int length) {
        if (mSourceDataCallback != null) {
            byte[] leftResult = new byte[length / 2];
            byte[] rightResult = new byte[length / 2];
            for (int i = 0; i < leftResult.length; i += 2) {
                leftResult[i] = data[i * 2];
                leftResult[i + 1] = data[i * 2 + 1];
                rightResult[i] = data[i * 2 + 2];
                rightResult[i + 1] = data[i * 2 + 3];
            }
            mSourceDataCallback.onSource(leftResult, rightResult);
        }
        mKikaBuffer.onData(data, length);
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

    public int checkFwVersion() {
        if (mAudioDriver != null) {
            return mAudioDriver.checkFwVersion();
        }
        return -1;
    }

    public int checkDriverVersion() {
        if (mAudioDriver != null) {
            return mAudioDriver.checkDriverVersion();
        }
        return -1;
    }

    public void setKikaBuffer(int tag) {
        if (!mIsOpened) {
            mKikaBuffer = KikaBuffer.getKikaBuffer(tag);
        } else {
            Logger.e("Can't change the buffer when it has been opened.");
        }
    }

    public void setSourceDataCallback(SourceDataCallback callback) {
        mSourceDataCallback = callback;
    }

    public int getNcVersion() {
        return KikaNcBuffer.getVersion();
    }
}
