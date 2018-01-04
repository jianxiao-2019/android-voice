package com.kikatech.go.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

/**
 * @author SkeeterWang Created on 2018/1/2.
 */

public class BluetoothUtil {
    private static final String TAG = "BluetoothUtil";

    public static final class Devices {
        public static final String DEVICE_KS = "KS-01";
        public static final String DEVICE_MEFOTO = "MEFOTO";
    }

    public static void printPairedDevices() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (LogUtil.DEBUG) {
            for (BluetoothDevice bluetoothDevice : pairedDevices) {
                LogUtil.logd(TAG, bluetoothDevice.getName());
            }
        }
    }
}
