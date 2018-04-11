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

    private static final int INIT_VOLUME = 6;
    private static final int ERROR_VOLUME = 255;

    private UsbAudio mUsbAudio = new UsbAudio();
    private OnDataListener mOnDataListener;
    public KikaAudioDriver(Context context, UsbDevice device) {
        super(context, device);
    }

    @Override
    public int open() {
        if (openConnection()) {
            Logger.d("KikaAudioDriver open openConnection  device name = "+ mDevice.getDeviceName()
                    + " mConnectionFileDes = " + mConnection.getFileDescriptor()
                    + " productId = " + mDevice.getProductId()
                    + " vendorId = " + mDevice.getVendorId());
            int result = mUsbAudio.setupWithChannelNo(
                    mDevice.getDeviceName(),
                    mConnection.getFileDescriptor(),
                    mDevice.getProductId(),
                    mDevice.getVendorId());
            Logger.d("KikaAudioDriver open result = " + result);
            if (result == RESULT_STEREO) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        mUsbAudio.loop();
                    }
                }).start();
            }
            int volume = mUsbAudio.checkVolumeState();
            while (volume != ERROR_VOLUME && volume != INIT_VOLUME) {
                if (volume > INIT_VOLUME) {
                    volume = mUsbAudio.volumeDown();
                } else if (volume < INIT_VOLUME) {
                    volume = mUsbAudio.volumeUp();
                }
            }
            return result;
        }
        Logger.w("Fail to connect the usb device.");
        return RESULT_CONNECTION_FAIL;
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
    }

    public int checkVolumeState() {
        Logger.d("[" + Thread.currentThread().getName() + "] checkVolumeState mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
        return mUsbAudio.checkVolumeState();
    }

    public int volumeUp() {
        Logger.d("[" + Thread.currentThread().getName() + "] volumeUp mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
        return mUsbAudio.volumeUp();
    }

    public int volumeDown() {
        Logger.d("[" + Thread.currentThread().getName() + "] volumeDown mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
        return mUsbAudio.volumeDown();
    }

    public int checkFwVersion() {
        byte[] result = mUsbAudio.checkFwVersion();

        return result[1] & 0xFF |
                (result[0] & 0xFF) << 8;
    }

    public int checkDriverVersion() {
        byte[] result = mUsbAudio.checkDriverVersion();

        return result[1] & 0xFF |
                (result[0] & 0xFF) << 8;
    }
}
