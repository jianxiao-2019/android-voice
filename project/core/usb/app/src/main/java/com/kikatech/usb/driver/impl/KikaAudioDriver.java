package com.kikatech.usb.driver.impl;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.kikatech.voice.util.log.Logger;
import com.xiao.usbaudio.AudioPlayBack;
import com.xiao.usbaudio.UsbAudio;

/**
 * Created by tianli on 17-11-18.
 */

public class KikaAudioDriver extends UsbHostDriver {

    private UsbAudio mUsbAudio = new UsbAudio();
    private OnDataListener mOnDataListener;
    public KikaAudioDriver(Context context, UsbDevice device) {
        super(context, device);
    }

    @Override
    public boolean open() {
        if (openConnection()) {
            Logger.d("KikaAudioDriver open openConnection  device name = "+ mDevice.getDeviceName()
                    + " mConnectionFileDes = " + mConnection.getFileDescriptor()
                    + " productId = " + mDevice.getProductId()
                    + " vendorId = " + mDevice.getVendorId());
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
            Logger.w("Fail to setup the usb audio device.");
        }
        Logger.w("Fail to connect the usb device.");
        return false;
    }

    @Override
    public void startRecording() {
        mUsbAudio.start();
        AudioPlayBack.setup(this);
    }

    @Override
    public void stopRecording() {
        mUsbAudio.stop();
        AudioPlayBack.stop();
    }

    @Override
    public void close() {
        super.close();
        mUsbAudio.close();
    }

    @Override
    public void setOnDataListener(OnDataListener listener) {
        mOnDataListener = listener;
    }

    public void onData(byte[] data, int length) {
        if (mOnDataListener != null) {
            mOnDataListener.onData(data, length);
        }
        Logger.d("onData mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
    }
}
