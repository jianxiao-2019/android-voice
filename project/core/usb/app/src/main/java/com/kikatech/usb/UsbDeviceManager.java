package com.kikatech.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

/**
 * Created by tianli on 17-11-6.
 */

class UsbDeviceManager {

    private static final String TAG = "UsbDeviceManager";
    private Context mContext;

    public UsbDeviceManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public boolean hasPermission(UsbDevice device) {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        return manager.hasPermission(device);
    }

}
