package com.kikatech.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.voice.util.log.Logger;

import static com.kikatech.usb.IUsbAudioListener.ERROR_DRIVER_CONNECTION_FAIL;
import static com.kikatech.usb.IUsbAudioListener.ERROR_NO_DEVICES;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbAudioService {

    private static final String TAG = "UsbAudioService";

    private static volatile UsbAudioService sInstance;

    private UsbAudioDriver mUsbAudioDriver = null;
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

    public void closeDevice() {
        if (mUsbAudioDriver != null) {
            mUsbAudioDriver.close();
            if (mListener != null) {
                mListener.onDeviceDetached();
            }
        }
    }

    public void setReqPermissionOnReceiver(boolean reqPermissionOnReceiver) {
        if (mDeviceManager != null) {
            mDeviceManager.setReqPermissionOnReceiver(reqPermissionOnReceiver);
        }
    }

    private UsbDeviceManager.IUsbAudioDeviceListener mDeviceListener = new UsbDeviceManager.IUsbAudioDeviceListener() {

        @Override
        public void onDeviceAttached(UsbDevice device) {
            Logger.i("UsbAudioService onDeviceAttached");
            if (mUsbAudioDriver != null) {
                mUsbAudioDriver.close();
            }

            mDevice = device;
            mUsbAudioDriver = new UsbAudioDriver(mContext, mDevice);
            if (mUsbAudioDriver.open()) {
                mAudioSource = new UsbAudioSource(mUsbAudioDriver);
                if (mListener != null) {
                    mListener.onDeviceAttached(mAudioSource);
                }
            } else {
                if (mListener != null) {
                    mListener.onDeviceError(ERROR_DRIVER_CONNECTION_FAIL);
                }
            }
        }

        @Override
        public void onDeviceDetached() {
            Logger.i("UsbAudioService onDeviceDetached");
            mDevice = null;
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
    };

}
