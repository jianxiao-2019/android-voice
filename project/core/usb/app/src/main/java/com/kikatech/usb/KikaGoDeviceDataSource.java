package com.kikatech.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.kikatech.voice.util.log.Logger;
import com.xiao.usbaudio.AudioPlayBack;
import com.xiao.usbaudio.UsbAudio;

/**
 * Created by ryanlin on 2018/5/6.
 */

public class KikaGoDeviceDataSource implements IUsbAudioDriver, IUsbDataSource {

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
        Logger.d("openUsb");
        try {
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            if (manager == null) {
                return false;
            }
            mConnection = manager.openDevice(mDevice);
            Logger.d("openUsb success = " + (mConnection != null));
            return mConnection != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void closeUsb() {
        Logger.d("closeUsb");
        if (mConnection != null) {
            mConnection.close();
        }
    }

    @Override
    public boolean open() {
        if (mConnection == null) {
            Logger.e("Should open usb first.");
        }
        Logger.d("KikaAudioDriver open openConnection  device name = " + mDevice.getDeviceName()
                + " mConnectionFileDes = " + mConnection.getFileDescriptor()
                + " productId = " + mDevice.getProductId()
                + " vendorId = " + mDevice.getVendorId());
        boolean success = mUsbAudio.setup(
                mDevice.getDeviceName(),
                mConnection.getFileDescriptor(),
                mDevice.getProductId(),
                mDevice.getVendorId());
        new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.v("KikaAudioDriver start loop");
                mUsbAudio.loop();
                Logger.v("KikaAudioDriver stop loop");
            }
        }).start();
        setToDefaultVolume();
        return success;
    }

    private void setToDefaultVolume() {
        int volume = mUsbAudio.checkVolumeState();
        while (volume != UsbAudioSource.ERROR_VOLUME_FW_NOT_SUPPORT && volume != UsbAudioSource.INIT_VOLUME) {
            if (volume > UsbAudioSource.INIT_VOLUME) {
                volume = mUsbAudio.volumeDown();
            } else if (volume < UsbAudioSource.INIT_VOLUME) {
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
        Logger.d("[" + Thread.currentThread().getName() + "] checkVolumeState mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
        return mUsbAudio.checkVolumeState();
    }

    @Override
    public int volumeUp() {
        Logger.d("[" + Thread.currentThread().getName() + "] volumeUp mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
        return mUsbAudio.volumeUp();
    }

    @Override
    public int volumeDown() {
        Logger.d("[" + Thread.currentThread().getName() + "] volumeDown mUsbAudio checkVolumeState = " + mUsbAudio.checkVolumeState());
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
