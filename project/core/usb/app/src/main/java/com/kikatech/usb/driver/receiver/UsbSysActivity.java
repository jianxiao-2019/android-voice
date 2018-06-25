package com.kikatech.usb.driver.receiver;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * an activity that receive system broadcast
 * with action: android.hardware.usb.action.USB_ACCESSORY_ATTACHED
 *
 * @author SkeeterWang Created on 2018/6/25.
 */

public final class UsbSysActivity extends Activity {
    private static final String TAG = "UsbSysActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UsbSysIntentProcessor.processIntent(TAG, getIntent());
        finish();
    }
}
