package com.kikatech.usb.driver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kikatech.usb.driver.receiver.UsbSysReceiver;
import com.kikatech.usb.util.DeviceUtil;
import com.kikatech.usb.util.LogUtil;

import java.util.HashMap;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbDeviceManager {
    private static final String TAG = "UsbDeviceManager";

    private Context mContext;

    private UsbDeviceReceiver mDeviceReceiver;
    private IUsbAudioDeviceListener mListener;

    public interface IUsbAudioDeviceListener {

        void onDeviceAttached(UsbDevice device);

        void onDeviceDetached();

        void onAccessoryAttached(UsbAccessory accessory);

        void onAccessoryDetached();

        void onNoDevices();
    }

    public UsbDeviceManager(Context context, IUsbAudioDeviceListener listener) {
        mContext = context.getApplicationContext();
        mDeviceReceiver = new UsbDeviceReceiver(mDeviceListener, mAccessoryListener);
        mDeviceReceiver.register();
        mListener = listener;
    }

    public void scanDevices() {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            mListener.onNoDevices();
            return;
        }

        if (scanUsbDevices(manager)) {
            return;
        }

        if (scanUsbAccessories(manager)) {
            return;
        }

        mListener.onNoDevices();
    }

    private boolean scanUsbDevices(UsbManager manager) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "scanUsbDevices");
        }
        HashMap<String, UsbDevice> allDeviceList = manager.getDeviceList();
        if (allDeviceList == null || allDeviceList.size() == 0) {
            return false;
        }
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("scanUsbDevices allDeviceList.size: %s", allDeviceList.values().size()));
        }
        for (UsbDevice device : allDeviceList.values()) {
            if (DeviceUtil.isKikaGoDevice(device)) {
                mDeviceListener.onUsbAttached(device);
                return true;
            }
        }

        return false;
    }

    private boolean scanUsbAccessories(@NonNull UsbManager manager) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "scanUsbAccessories");
        }
        UsbAccessory[] accessories = manager.getAccessoryList();
        if (accessories == null || accessories.length == 0) {
            return false;
        }
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("scanUsbAccessories accessories.size: %s", accessories.length));
        }
        for (UsbAccessory accessory : accessories) {
            if (DeviceUtil.isKikaGoAccessory(accessory)) {
                mAccessoryListener.onUsbAttached(accessory);
                return true;
            }
        }

        return false;
    }

    private UsbDeviceReceiver.UsbDeviceListener mDeviceListener
            = new UsbDeviceReceiver.UsbDeviceListener() {
        @Override
        public void onUsbAttached(UsbDevice device) {
            Log.d(TAG, "onUsbAttached device: " + device);
            if (hasPermission(device)) {
                mListener.onDeviceAttached(device);
            } else {
                requestPermission(device);
            }
        }

        @Override
        public void onUsbDetached(UsbDevice device) {
            mListener.onDeviceDetached();
        }

        @Override
        public void onUsbPermissionGranted(UsbDevice device) {
            mListener.onDeviceAttached(device);
        }
    };

    private UsbDeviceReceiver.UsbAccessoryListener mAccessoryListener
            = new UsbDeviceReceiver.UsbAccessoryListener() {
        @Override
        public void onUsbAttached(UsbAccessory accessory) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onUsbAttached accessory = " + accessory);
            }
            mListener.onAccessoryAttached(accessory);
//    TODO: check if request accessory permission is needed in different android version
//            if (hasPermission(accessory)) {
//                mListener.onAccessoryAttached(accessory);
//            } else {
//                requestPermission(accessory);
//            }
        }

        @Override
        public void onUsbDetached(@NonNull UsbAccessory accessory) {
            mListener.onAccessoryDetached();
        }
    };

    private boolean hasPermission(UsbDevice device) {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        return manager != null && manager.hasPermission(device);
    }

//    TODO: check if request accessory permission is needed in different android version
//    private boolean hasPermission(UsbAccessory accessory) {
//        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
//        return manager != null && manager.hasPermission(accessory);
//    }

    private void requestPermission(UsbDevice device) {
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(UsbSysReceiver.ACTION_USB_DEVICE_PERMISSION_GRANTED), 0);
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (device != null && manager != null && intent != null) {
            manager.requestPermission(device, intent);
        } else {
            Log.e(TAG, "requestPermission exception.");
        }
    }

//    TODO: check if request accessory permission is needed in different android version
//    private void requestPermission(UsbAccessory accessory) {
//        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0,
//                new Intent(UsbSysReceiver.ACTION_USB_DEVICE_PERMISSION_GRANTED), 0);
//        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
//        if (accessory != null && manager != null && intent != null) {
//            manager.requestPermission(accessory, intent);
//        } else {
//            Log.e(TAG, "requestPermission exception.");
//        }
//    }

}
