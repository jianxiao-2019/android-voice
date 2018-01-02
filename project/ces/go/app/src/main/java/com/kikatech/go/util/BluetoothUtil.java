package com.kikatech.go.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.InputDevice;
import android.view.KeyEvent;

import java.util.Set;

/**
 * @author SkeeterWang Created on 2018/1/2.
 */

public class BluetoothUtil {
    private static final String TAG = "BluetoothUtil";

    public static final String DEVICE_NAME = "KS-01";

    public static void printPairedDevices() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (LogUtil.DEBUG) {
            for (BluetoothDevice bluetoothDevice : pairedDevices) {
                LogUtil.logd(TAG, bluetoothDevice.getName());
            }
        }
    }

    public static boolean isTargetDeviceEvent(KeyEvent event) {
        if (event != null) {
            InputDevice device = event.getDevice();
            return device != null && DEVICE_NAME.equals(device.getName());
        }
        return false;
    }
}
