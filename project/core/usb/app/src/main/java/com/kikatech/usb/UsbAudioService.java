package com.kikatech.usb;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbAudioService {

    private static final String TAG = "UsbAudioService";

    private static volatile UsbAudioService sInstance;

    private Context mContext;
    private IUsbAudioListener mListener;
    private UsbDeviceReceiver mDeviceReceiver;
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
        mDeviceReceiver = new UsbDeviceReceiver(mDeviceListener);
        mDeviceReceiver.register(context);
        mDeviceManager = new UsbDeviceManager(mContext);
    }

    public void setListener(IUsbAudioListener listener) {
        mListener = listener;
    }

    public void scanDevices(){
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if(deviceList != null && deviceList.values() != null){
            Iterator<UsbDevice> iterator = deviceList.values().iterator();
            while(iterator.hasNext()){
                UsbDevice device = iterator.next();
                if(device.getInterfaceCount() > 0){
                    UsbInterface usbInterface = device.getInterface(0);
                    Log.d(TAG, "Audio UsbInterface : " + usbInterface.getInterfaceClass());
                    if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO) {
                        Log.d(TAG, "Audio class device: " + device);
                        mDevice = device;
                        Log.d(TAG, "Audio class device name: " + mDevice.getDeviceName());
                        mDeviceListener.onUsbAttached(device);
                        return;
                    }
                }
            }
        }
    }

    public void startForegroundService() {
    }

    private UsbDeviceReceiver.UsbDeviceListener mDeviceListener = new UsbDeviceReceiver.UsbDeviceListener() {
        @Override
        public void onUsbAttached(UsbDevice device) {
            if (device != null) {
                if(mDeviceManager.hasPermission(device)){

                }else{
                    mDeviceManager.requestPermission(device, mDeviceReceiver);
                }
            }
        }

        @Override
        public void onUsbDetached(UsbDevice device) {
            if (device != null) {
            }
        }

        @Override
        public void onUsbPermissionGranted(UsbDevice device) {
            if (device != null) {
            }
        }
    };
}
