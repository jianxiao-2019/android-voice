package com.kikatech.usb;

/**
 * Created by ryanlin on 2018/5/9.
 */

public interface IUsbAudioDriver {

    boolean openUsb();

    void closeUsb();
}
