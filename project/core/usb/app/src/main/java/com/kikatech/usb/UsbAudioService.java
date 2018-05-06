package com.kikatech.usb;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;

import com.kikatech.voice.util.log.Logger;

import static com.kikatech.usb.IUsbAudioListener.ERROR_DRIVER_CONNECTION_FAIL;
import static com.kikatech.usb.IUsbAudioListener.ERROR_NO_DEVICES;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbAudioService {

    private static volatile UsbAudioService sInstance;

    private IUsbAudioDriver mUsbAudioDriver = null;

    private Context mContext;
    private IUsbAudioListener mListener;
    private UsbDeviceManager mDeviceManager;

    public static UsbAudioService getInstance(Context context) {
        if (sInstance == null) {
            synchronized (UsbDeviceManager.class) {
                if (sInstance == null) {
                    sInstance = new UsbAudioService(context);
                }
            }
        }
        return sInstance;
    }

    private UsbAudioService(Context context) {
        mContext = context.getApplicationContext();
        mDeviceManager = new UsbDeviceManager(mContext, mDeviceListener);
    }

    public void setListener(IUsbAudioListener listener) {
        mListener = listener;
    }

    public void scanDevices() {
        mDeviceManager.scanDevices();
    }

    public void closeDevice() {
        if (mUsbAudioDriver != null) {
            mUsbAudioDriver.closeUsb();
            if (mListener != null) {
                mListener.onDeviceDetached();
            }
        }
    }

    private UsbDeviceManager.IUsbAudioDeviceListener mDeviceListener = new UsbDeviceManager.IUsbAudioDeviceListener() {

        @Override
        public void onDeviceAttached(UsbDevice device) {
            Logger.i("UsbAudioService onDeviceAttached");
            if (mUsbAudioDriver != null) {
                mUsbAudioDriver.closeUsb();
            }

            onDeviceAttached(new KikaGoDeviceDataSource(mContext, device));
        }

        @Override
        public void onDeviceDetached() {
            Logger.i("UsbAudioService onDeviceDetached");
            if (mUsbAudioDriver != null) {
                mUsbAudioDriver = null;
                if (mListener != null) {
                    mListener.onDeviceDetached();
                }
            }
        }

        @Override
        public void onAccessoryAttached(UsbAccessory accessory) {
            Logger.i("UsbAudioService onDeviceAttached");
            if (mUsbAudioDriver != null) {
                mUsbAudioDriver.closeUsb();
            }

            onDeviceAttached(new KikaGoAccessoryDataSource(mContext, accessory));
        }

        @Override
        public void onAccessoryDetached() {
            Logger.i("UsbAudioService onAccessoryDetached");
            if (mUsbAudioDriver != null) {
                mUsbAudioDriver = null;
                if (mListener != null) {
                    mListener.onDeviceDetached();
                }
            }
        }

        @Override
        public void onNoDevices() {
            if (mListener != null) {
                mListener.onDeviceError(ERROR_NO_DEVICES);
            }
        }

        private void onDeviceAttached(IUsbAudioDriver driver) {
            mUsbAudioDriver = driver;
            if (mUsbAudioDriver.openUsb()) {
                UsbAudioSource mAudioSource = new UsbAudioSource((IUsbDataSource) mUsbAudioDriver);
                if (mListener != null) {
                    mListener.onDeviceAttached(mAudioSource);
                }
            } else {
                if (mListener != null) {
                    mListener.onDeviceError(ERROR_DRIVER_CONNECTION_FAIL);
                }
            }
        }
    };

}
