package com.kikatech.usb.driver.impl;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;

import com.xiao.usbaudio.UsbAudio;

/**
 * Created by tianli on 17-11-18.
 */

public class KikaAudioDriver extends UsbHostAudioDriver {

    private UsbAudio mUsbAudio = new UsbAudio();

    public KikaAudioDriver(Context context, UsbDevice device) {
        super(context, device);
    }

    @Override
    public boolean open() {
        if (openConnection()) {
            if (mUsbAudio.setup(mDevice.getDeviceName(), mConnection.getFileDescriptor(),
                    mDevice.getProductId(), mDevice.getVendorId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void startRecording() {
        mUsbAudio.loop();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return 0;
    }

    @Override
    public void stopRecording() {
        mUsbAudio.stop();
    }

    @Override
    public void close() {
        mUsbAudio.close();
    }
}
