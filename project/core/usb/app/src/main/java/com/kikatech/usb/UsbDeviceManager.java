package com.kikatech.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.kikatech.usb.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tianli on 17-11-6.
 */

class UsbDeviceManager {

    private static final String TAG = "UsbDeviceManager";
    private Context mContext;
    private UsbDevice mDevice;
    private UsbDeviceReceiver mDeviceReceiver;
    private IUsbAudioDeviceListener mListener = null;

    public UsbDeviceManager(Context context, IUsbAudioDeviceListener listener) {
        mContext = context.getApplicationContext();
        mDeviceReceiver = new UsbDeviceReceiver(mDeviceListener);
        mDeviceReceiver.register(context);
        mListener = listener;
    }

    public boolean hasPermission(UsbDevice device) {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        return manager.hasPermission(device);
    }

    public void requestPermission(UsbDevice device, BroadcastReceiver receiver) {
        // Register for permission
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(UsbDeviceReceiver.ACTION_USB_PERMISSION_GRANTED), 0);
        if (device != null && intent != null) {
            IntentFilter filter = new IntentFilter(UsbDeviceReceiver.ACTION_USB_PERMISSION_GRANTED);
            mContext.registerReceiver(receiver, filter);
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            manager.requestPermission(device, intent);
        } else {
            Log.e(TAG, "requestPermission exception.");
        }
    }

    public void scanDevices() {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if (deviceList != null && deviceList.values() != null) {
            Iterator<UsbDevice> iterator = deviceList.values().iterator();
            while (iterator.hasNext()) {
                UsbDevice device = iterator.next();
                mDeviceListener.onUsbAttached(device);
            }
        }
    }

    private boolean isAudioDevice(UsbDevice device) {
        if (device != null && device.getInterfaceCount() > 0) {
            UsbInterface usbInterface = device.getInterface(0);
            Log.d(TAG, "Audio UsbInterface : " + usbInterface.getInterfaceClass());
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO) {
                return true;
            }
//            Iterator<?> it = devices.entrySet().iterator();
//            while (it.hasNext()) {
//                @SuppressWarnings("rawtypes") Map.Entry pairs = (Map.Entry) it.next();
//                UsbDevice dev = (UsbDevice) pairs.getValue();
//
//                if (dev != null) {
//                    int deviceClass = dev.getDeviceClass();
//
//                    if (((deviceClass == 1) || (deviceClass == 0) || (deviceClass == 239) || (deviceClass == 255)) && // inspect interface device class, misc = 239, 255 = vendor specific
//                            dev.getVendorId() != 0x05C6 && dev.getVendorId() != 0x05E1 && // Symantec bluetooth and video cameras
//                            dev.getVendorId() != 0x0A5C) // Broadcom (bluetooth)
//                    {
//                        m_devicesToQuery++;
//                    }
//
//                    if (dev.getVendorId() == 0x1519 && dev.getProductId() == 0x443) // Samsung dock?
//                    {
//                        //appendLog(m_context, "   hasClass2Vendor5401");
//                        hasClass2Vendor5401 = true;
//                    }
//                }
//            }
//            if (LogUtil.DEBUG)
//                LogUtil.log(TAG, "Devices to query = " + m_devicesToQuery + ", hasClass2Vendor5401 = " + hasClass2Vendor5401);
//
//            it = devices.entrySet().iterator();
//            while (it.hasNext()) {
//                @SuppressWarnings("rawtypes") Map.Entry pairs = (Map.Entry) it.next();
//                UsbDevice dev = (UsbDevice) pairs.getValue();
//
//                if (dev != null) {
//                    int deviceClass = dev.getDeviceClass();
//                    //Log.v(TAG, "deviceClass = " + deviceClass + ", dev.getVendorId() = " + dev.getVendorId());
//                    if (LogUtil.DEBUG)
//                        LogUtil.log(TAG, "deviceClass = " + deviceClass + ", dev.getVendorId() = " + dev.getVendorId());
//
//                    if (((deviceClass == 1) || (deviceClass == 0) || (deviceClass == 239) || (deviceClass == 255)) && dev.getVendorId() != 0x05C6 && dev.getVendorId() != 0x05E1 && // Symantec bluetooth and video cameras
//                            dev.getVendorId() != 0x0A5C) {
//                        if (hasClass2Vendor5401 && dev.getVendorId() == 0x8BB && m_devicesToQuery >= 2) {
//                            if (LogUtil.DEBUG) LogUtil.logw(TAG, "Skipping dock audio!");
//                            continue;
//                        } else {
//                            //appendLog(m_context, "Not skipping: hasClass2Vendor5401 = " + hasClass2Vendor5401 + ", dev.getVendorId() = " + dev.getVendorId() + ", m_devicesToQuery = " + m_devicesToQuery);
//                        }
//
//                        possibleAudioDevices++;
//                        //Log.v(TAG, "----> requestPermission");
//                        if (LogUtil.DEBUG) LogUtil.log(TAG, "requestPermission");
//
//                        if (manager.hasPermission(dev) == false) {
//                            manager.requestPermission(dev, mPermissionIntent);
//                        } else {
//                            openDevice(mContext, dev);
//                        }
//                    }
//                }
//            }
        }
        return false;
    }

    private UsbDeviceReceiver.UsbDeviceListener mDeviceListener = new UsbDeviceReceiver.UsbDeviceListener() {
        @Override
        public void onUsbAttached(UsbDevice device) {
            if (isAudioDevice(device)) {
                Log.d(TAG, "Audio class device: " + device);
                Log.d(TAG, "Audio class device name: " + mDevice.getDeviceName());
                if (hasPermission(device)) {
                    mDevice = device;
                    mListener.onDeviceAttached(mDevice);
                } else {
                    requestPermission(device, mDeviceReceiver);
                }
            }
        }

        @Override
        public void onUsbDetached(UsbDevice device) {
            if (device != null) {
                int detachedVendorId = device.getVendorId();
                int attachedVendorId = mDevice.getVendorId();
                int detachedProductId = device.getProductId();
                int attachedProductId = mDevice.getProductId();
                Log.d(TAG, "detachedDeviceVendorId: " + detachedVendorId);
                Log.d(TAG, "attachedDeviceVendorId: " + attachedVendorId);
                Log.d(TAG, "detachedProductId: " + detachedProductId);
                Log.d(TAG, "attachedProductId: " + attachedProductId);
                // TODO: 17-11-18 handle usb detach
                if (detachedVendorId == attachedVendorId && detachedProductId == attachedProductId) {
                    mDevice = null;
                    mListener.onDeviceDetached();
                }
            }
        }

        @Override
        public void onUsbPermissionGranted(UsbDevice device) {
            if (device != null) {
                mDevice = device;
                mListener.onDeviceAttached(mDevice);
            }
        }
    };

    interface IUsbAudioDeviceListener {

        void onDeviceAttached(UsbDevice device);

        void onDeviceDetached();
    }
}
