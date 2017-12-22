package com.kikatech.usb;

import android.support.annotation.NonNull;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbAudioSource implements IVoiceSource {

    public static final int READ_FAIL = -99;

    private UsbAudioDriver mAudioDriver;

    public UsbAudioSource(UsbAudioDriver driver) {
        mAudioDriver = driver;
    }

    @Override
    public void start() {
        if(mAudioDriver != null)
            mAudioDriver.startRecording();
    }

    @Override
    public void stop() {
        if(mAudioDriver != null)
            mAudioDriver.stopRecording();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if(mAudioDriver != null)
            return mAudioDriver.read(audioData, offsetInBytes, sizeInBytes);
        return READ_FAIL;
    }

    @Override
    public int getBufferSize() {
        return 640;
    }

    @Override
    public boolean isStereo() {
        return true;
    }

    public void close() {
        if (mAudioDriver != null) {
            mAudioDriver.close();
            mAudioDriver = null;
        }
    }
}
