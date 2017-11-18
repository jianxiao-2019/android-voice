package com.kikatech.usb.driver.impl;

import android.support.annotation.NonNull;

import com.kikatech.usb.driver.UsbAudioDriver;

/**
 * Created by tianli on 17-11-18.
 */

public class KikaAudioDriver implements UsbAudioDriver {

    @Override
    public boolean open() {
        return false;
    }

    @Override
    public void startRecording() {

    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return 0;
    }

    @Override
    public void stopRecording() {

    }

    @Override
    public void close() {

    }
}
