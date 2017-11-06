package com.kikatech.usb.device;

import android.content.Context;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbDeviceManager {

    private static final String TAG = "UsbDeviceManager";
    private static volatile UsbDeviceManager sInstance;

    private Context mContext;

    public static UsbDeviceManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (UsbDeviceManager.class){
                if(sInstance == null){
                    sInstance = new UsbDeviceManager(context);
                }
            }
        }
        return sInstance;
    }

    private UsbDeviceManager(Context context){
        mContext = context.getApplicationContext();
    }

}
