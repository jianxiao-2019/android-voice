package com.kikatech.usb;

import android.support.annotation.NonNull;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.util.log.Logger;

import java.util.Objects;

/**
 * Created by tianli on 17-11-6.
 * Update by ryanlin on 25/12/2017.
 */

public class UsbAudioSource implements IVoiceSource {

    public static final int READ_FAIL = -99;

    private UsbAudioDriver mAudioDriver;

    public UsbAudioSource(UsbAudioDriver driver) {
        mAudioDriver = driver;
    }

    @Override
    public void open() {

    }

    @Override
    public void start() {
        if (mAudioDriver != null) {
            mAudioDriver.startRecording();
        } else {
            Logger.w("Don't call start() after close().");
        }
    }

    @Override
    public void stop() {
        if (mAudioDriver != null) {
            mAudioDriver.stopRecording();
        } else {
            Logger.w("Don't call stop() after close().");
        }
    }

    @Override
    public void close() {
        if (mAudioDriver != null) {
            mAudioDriver.close();
            mAudioDriver = null;
        }
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (mAudioDriver == null) {
            Logger.w("Don't call read() after close().");
            return READ_FAIL;
        }
        return mAudioDriver.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public int getBufferSize() {
        // TODO : Magic number.
        return 640;
    }
}
