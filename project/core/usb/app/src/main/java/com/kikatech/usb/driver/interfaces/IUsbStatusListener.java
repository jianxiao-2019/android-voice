package com.kikatech.usb.driver.interfaces;

import com.kikatech.usb.driver.USBDeviceManager;

/**
 * @author SkeeterWang Created on 2017/10/26.
 */
public interface IUsbStatusListener extends USBDeviceManager.IUsbDriverListener {
    void onServiceStarted();

    void onServiceStopped();
}
