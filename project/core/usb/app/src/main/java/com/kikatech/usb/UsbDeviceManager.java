package com.kikatech.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.xiao.usbaudio.USBAudioActivity;

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

    public void requestPermission(UsbDevice device, BroadcastReceiver receiver) {
        // Register for permission
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(UsbDeviceReceiver.ACTION_USB_PERMISSION_GRANTED), 0);
        if(device != null && intent != null){
            IntentFilter filter = new IntentFilter(UsbDeviceReceiver.ACTION_USB_PERMISSION_GRANTED);
            mContext.registerReceiver(receiver, filter);
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            manager.requestPermission(device, intent);
        }else{
            Log.e(TAG, "requestPermission exception.");
        }
    }

}
