package com.kikatech.usb.datasource;

/**
 * Created by ryanlin on 2018/5/6.
 */

public interface IUsbDataSource {

    interface OnDataListener {
        void onData(byte[] data, int length);
    }

    boolean open();

    void start();

    void stop();

    void close();

    int checkVolumeState();

    int volumeUp();

    int volumeDown();

    byte[] checkFwVersion();

    byte[] checkDriverVersion();

    String getSn();

    void setOnDataListener(OnDataListener listener);
}
