package com.kikatech.usb.datasource.impl;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.kikatech.usb.datasource.IUsbDataSource;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.usb.driver.IUsbAudioDriver;
import com.kikatech.usb.util.LogUtil;

import ai.kikago.usb.AudioPlayBack;
import ai.kikago.usb.NoiseCancellation;
import ai.kikago.usb.UsbAudio;

/**
 * Created by ryanlin on 2018/5/6.
 */

public class KikaGoDeviceDataSource implements IUsbAudioDriver, IUsbDataSource {
    private static final String TAG = "KikaGoDeviceDataSource";

    private UsbAudio mUsbAudio = new UsbAudio();

    private Context mContext;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection = null;

    private OnDataListener mListener = null;

    public KikaGoDeviceDataSource(Context context, UsbDevice device) {
        mContext = context.getApplicationContext();
        mDevice = device;
    }

    @Override
    public boolean openUsb() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "openUsb");
        }
        try {
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            if (manager == null) {
                return false;
            }
            mConnection = manager.openDevice(mDevice);
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, String.format("openUsb success: %s", String.valueOf(mConnection != null)));
            }
            return mConnection != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void closeUsb() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "closeUsb");
        }
        if (mConnection != null) {
            mConnection.close();
        }
    }

    @Override
    public boolean open() {
        if (mConnection == null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Should open usb first.");
            }
        }
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "KikaAudioDriver open openConnection  device name = " + mDevice.getDeviceName()
                    + " mConnectionFileDes = " + mConnection.getFileDescriptor()
                    + " productId = " + mDevice.getProductId()
                    + " vendorId = " + mDevice.getVendorId());
        }
        boolean success = mUsbAudio.setup(
                mDevice.getDeviceName(),
                mConnection.getFileDescriptor(),
                mDevice.getProductId(),
                mDevice.getVendorId());
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("KikaAudioDriver open success: %s", success));
        }
        if (success) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(-20);
                    if (LogUtil.DEBUG) {
                        LogUtil.logv(TAG, "KikaAudioDriver start loop");
                    }
                    mUsbAudio.loop();
                    if (LogUtil.DEBUG) {
                        LogUtil.logv(TAG, "KikaAudioDriver stop loop");
                    }
                }
            }).start();
            setToDefaultVolume();
        }
        return success;
    }

    private void setToDefaultVolume() {
        int volume = mUsbAudio.checkVolumeState();
        while (volume != KikaGoVoiceSource.ERROR_VOLUME_FW_NOT_SUPPORT && volume != KikaGoVoiceSource.INIT_VOLUME) {
            if (volume > KikaGoVoiceSource.INIT_VOLUME) {
                volume = mUsbAudio.volumeDown();
            } else if (volume < KikaGoVoiceSource.INIT_VOLUME) {
                volume = mUsbAudio.volumeUp();
            }
        }
    }

    @Override
    public void start() {
        AudioPlayBack.setup(this);
        mUsbAudio.start();
    }

    @Override
    public void stop() {
        mUsbAudio.stop();
        AudioPlayBack.stop();
    }

    @Override
    public void close() {
        mUsbAudio.close();
    }

    @Override
    public int checkVolumeState() {
        int volumeState = mUsbAudio.checkVolumeState();
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("[%s] checkVolumeState mUsbAudio checkVolumeState: %s", Thread.currentThread().getName(), volumeState));
        }
        return volumeState;
    }

    @Override
    public int volumeUp() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("[%s] volumeUp mUsbAudio checkVolumeState: %s", Thread.currentThread().getName(), mUsbAudio.checkVolumeState()));
        }
        return mUsbAudio.volumeUp();
    }

    @Override
    public int volumeDown() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("[%s] volumeDown mUsbAudio checkVolumeState: %s", Thread.currentThread().getName(), mUsbAudio.checkVolumeState()));
        }
        return mUsbAudio.volumeDown();
    }

    @Override
    public byte[] checkFwVersion() {
        byte[] result = mUsbAudio.checkFwVersion();
        return result;
    }

    @Override
    public byte[] checkDriverVersion() {
        byte[] result = mUsbAudio.checkDriverVersion();
        return result;
    }

    @Override
    public void setOnDataListener(OnDataListener listener) {
        mListener = listener;
    }

    public void onData(byte[] data, int length) {
        if (mListener != null) {
            mListener.onData(data, length);
        }
    }
}
