package com.kikatech.usb.driver.impl;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.kikatech.usb.driver.UsbAudioDriver;

/**
 * Created by tianli on 17-11-18.
 */

public abstract class UsbHostDriver implements UsbAudioDriver {

    protected Context mContext;
    protected UsbDeviceConnection mConnection = null;
    protected UsbDevice mDevice;

    public UsbHostDriver(Context context, UsbDevice device) {
        mContext = context.getApplicationContext();
        mDevice = device;
    }

    protected boolean openConnection() {
        try {
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            mConnection = manager.openDevice(mDevice);
            return mConnection != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() {
        if (mConnection != null) {
            mConnection.close();
        }
    }
}
