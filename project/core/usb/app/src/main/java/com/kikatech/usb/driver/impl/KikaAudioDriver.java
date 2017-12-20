package com.kikatech.usb.driver.impl;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;

import com.kikatech.voice.util.log.Logger;
import com.xiao.usbaudio.AudioPlayBack;
import com.xiao.usbaudio.UsbAudio;

/**
 * Created by tianli on 17-11-18.
 */

public class KikaAudioDriver extends UsbHostDriver {

    private UsbAudio mUsbAudio = new UsbAudio();

    private AudioBuffer mAudioBuffer = new AudioBuffer(20000);

    public KikaAudioDriver(Context context, UsbDevice device) {
        super(context, device);
    }

    @Override
    public boolean open() {
        Logger.d("KikaAudioDriver open");
        if (openConnection()) {
            Logger.d("KikaAudioDriver open openConnection  device name = " + mDevice.getDeviceName() + " mConnectionFileDes = " + mConnection.getFileDescriptor() + " productId = " + mDevice.getProductId() + " vendorId = " + mDevice.getVendorId());
            if (mUsbAudio.setup(
                    mDevice.getDeviceName(),
                    mConnection.getFileDescriptor(),
                    mDevice.getProductId(),
                    mDevice.getVendorId())) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        mUsbAudio.loop();
                    }
                }).start();
                return true;
            }
            Logger.d("KikaAudioDriver open setup fail.");
        }
        Logger.d("KikaAudioDriver open setup fail 2.");
        return false;
    }

    @Override
    public void startRecording() {
        AudioPlayBack.setup(mAudioBuffer);
        mUsbAudio.start();
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return mAudioBuffer.read(audioData, offsetInBytes, sizeInBytes);
    }

    @Override
    public void stopRecording() {
        mUsbAudio.stop();
    }

    @Override
    public void close() {
        super.close();
        mUsbAudio.close();
    }
}
