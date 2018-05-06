package com.kikatech.usb.util;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;

/**
 * @author SkeeterWang Created on 2018/4/20.
 */

public class DeviceUtil {
    private static final String TAG = "DeviceUtil";

    private static final int KIKA_GO_VENDER_ID = 1037;
    private static final int KIKA_GO_PRODUCT_ID = 13323;

    private static final String KIKA_GO_MODEL = "WCHUARTDemo";
    private static final String KIKA_GO_MANUFACTURE = "WCH";

    public static boolean isKikaGoDevice(UsbDevice device) {
        return device != null
                && device.getVendorId() == KIKA_GO_VENDER_ID
                && device.getProductId() == KIKA_GO_PRODUCT_ID;
    }

    public static boolean isKikaGoAccessory(UsbAccessory accessory) {
        return accessory != null
                && KIKA_GO_MODEL.equals(accessory.getModel())
                && KIKA_GO_MANUFACTURE.equals(accessory.getManufacturer());
    }
}
