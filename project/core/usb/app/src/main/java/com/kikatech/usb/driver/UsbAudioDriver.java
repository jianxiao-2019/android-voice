package com.kikatech.usb.driver;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbAudioDriver {

    private Context mContext;

    private UsbDeviceConnection mConnection = null;
    private UsbDevice mDevice;

    public UsbAudioDriver(Context context, UsbDevice device) {
        mContext = context.getApplicationContext();
        mDevice = device;
    }

    public boolean open() {
        try {
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            if (manager == null) {
                return false;
            }
            mConnection = manager.openDevice(mDevice);
            return mConnection != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void close() {
        if (mConnection != null) {
            mConnection.close();
        }
    }

    public String getDeviceName() {
        return mDevice.getDeviceName();
    }

    public int getFileDescriptor() {
        return mConnection.getFileDescriptor();
    }

    public int getProductId() {
        return mDevice.getProductId();
    }

    public int getVendorId() {
        return mDevice.getVendorId();
    }
}
