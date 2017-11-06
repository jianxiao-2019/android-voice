package com.kikatech.usb;

import android.support.annotation.NonNull;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.voice.core.recorder.IVoiceSource;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbAudioRecord implements IVoiceSource{

    private UsbAudioDriver mAudioDriver;

    public UsbAudioRecord(UsbAudioDriver driver){
        mAudioDriver = driver;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return 0;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }
}
