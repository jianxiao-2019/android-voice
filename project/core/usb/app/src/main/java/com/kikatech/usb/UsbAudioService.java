package com.kikatech.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.kikatech.usb.driver.UsbAudioDriver;
import com.kikatech.usb.driver.impl.KikaAudioDriver;

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

    public void close() {
        if (mAudioSource != null) {
            mAudioSource.close();
        }
    }

    private UsbDeviceManager.IUsbAudioDeviceListener mDeviceListener = new UsbDeviceManager.IUsbAudioDeviceListener() {
        @Override
        public void onDeviceAttached(UsbDevice device) {
            mDevice = device;
            UsbAudioDriver driver = new KikaAudioDriver(mContext, mDevice);
            if (driver.open()) {
                mAudioSource = new UsbAudioSource(new KikaAudioDriver(mContext, mDevice));
                mListener.onDeviceAttached(mAudioSource);
            } else {
                // TODO: 17-11-20 handle exception
            }
        }

        @Override
        public void onDeviceDetached() {
            mDevice = null;
            if (mAudioSource != null) {
                mAudioSource.close();
            }
            if (mAudioSource != null) {
                mAudioSource = null;
                mListener.onDeviceDetached();
            }
        }
    };

}
