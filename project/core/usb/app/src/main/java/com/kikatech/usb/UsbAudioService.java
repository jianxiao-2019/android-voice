package com.kikatech.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;

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

    public void startForegroundService() {
    }

    private UsbDeviceReceiver.UsbDeviceListener mDeviceListener = new UsbDeviceReceiver.UsbDeviceListener() {
        @Override
        public void onUsbAttached(UsbDevice device) {
            if (device != null) {
            }
        }

        @Override
        public void onUsbDetached(UsbDevice device) {
            if (device != null) {
            }
        }

        @Override
        public void onUsbPermissionGrant(UsbDevice device) {
            if (device != null) {
            }
        }
    };
}
