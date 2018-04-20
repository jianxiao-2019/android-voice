package com.kikatech.usb.util;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author SkeeterWang Created on 2018/4/20.
 */

public class DeviceUtil {
    private static final String TAG = "DeviceUtil";

    public static boolean isAudioDevice(UsbDevice device) {
        if (device != null && device.getInterfaceCount() > 0) {
            UsbInterface usbInterface = device.getInterface(0);
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, String.format("Audio UsbInterface: %s", usbInterface.getInterfaceClass()));
            }
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO) {
                return true;
            }
        }
        return false;
    }

    public static List<UsbDevice> getAduioDeviceList(@NonNull HashMap<String, UsbDevice> allDeviceList) {
        List<UsbDevice> result = new ArrayList<>();
        for (UsbDevice device : allDeviceList.values()) {
            if (isAudioDevice(device)) {
                result.add(device);
            }
        }
        return result;
    }
}
