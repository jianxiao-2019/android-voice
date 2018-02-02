package com.kikatech.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.usb.driver.impl.KikaAudioDriver;
import com.kikatech.voice.util.log.Logger;

import static com.kikatech.usb.IUsbAudioListener.ERROR_DRIVER_INIT_FAIL;
import static com.kikatech.usb.IUsbAudioListener.ERROR_NO_DEVICES;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbAudioService {

    private static final String TAG = "UsbAudioService";

    private static volatile UsbAudioService sInstance;

    private UsbAudioSource mAudioSource = null;
    private Context mContext;
    private IUsbAudioListener mListener;
    private UsbDeviceManager mDeviceManager;
    private UsbDevice mDevice;

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

    private UsbDeviceManager.IUsbAudioDeviceListener mDeviceListener = new UsbDeviceManager.IUsbAudioDeviceListener() {

        @Override
        public void onDeviceAttached(UsbDevice device) {
            mDevice = device;
            UsbAudioDriver driver = new KikaAudioDriver(mContext, mDevice);
            if (driver.open()) {
                mAudioSource = new UsbAudioSource(driver);
                if (mListener != null) {
                    mListener.onDeviceAttached(mAudioSource);
                }
            } else {
                if (mListener != null) {
                    mListener.onDeviceError(ERROR_DRIVER_INIT_FAIL);
                }
            }
        }

        @Override
        public void onDeviceDetached() {
            mDevice = null;
            if (mAudioSource != null) {
                mAudioSource.closeDevice();
                mAudioSource = null;
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
    };

}
