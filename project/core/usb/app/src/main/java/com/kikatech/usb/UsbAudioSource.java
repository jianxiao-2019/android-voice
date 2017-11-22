package com.kikatech.usb;

import android.support.annotation.NonNull;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbAudioSource implements IVoiceSource {

    private UsbAudioDriver mAudioDriver;

    public UsbAudioSource(UsbAudioDriver driver) {
        mAudioDriver = driver;
    }

    @Override
    public void start() {
        mAudioDriver.startRecording();
    }

    @Override
    public void stop() {
        mAudioDriver.stopRecording();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mAudioDriver.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public int getBufferSize() {
        return 1960;
    }

    public void close() {
        if (mAudioDriver != null) {
            mAudioDriver.close();
        }
    }
}
