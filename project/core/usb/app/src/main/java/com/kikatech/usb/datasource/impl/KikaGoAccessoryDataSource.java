package com.kikatech.usb.datasource.impl;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import com.kikatech.usb.datasource.IUsbDataSource;
import com.kikatech.usb.driver.IUsbAudioDriver;
import com.kikatech.voice.util.log.Logger;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import static com.kikatech.usb.datasource.KikaGoVoiceSource.ERROR_VOLUME_FW_NOT_SUPPORT;

/**
 * Created by ryanlin on 2018/5/9.
 */

public class KikaGoAccessoryDataSource implements IUsbAudioDriver, IUsbDataSource {

    private Context mContext;
    private UsbAccessory mUsbAccessory;

    private ParcelFileDescriptor mParcelFileDescriptor;
    private FileInputStream mInputStream = null;

    private OnDataListener mListener;

    private final Object mSyncObj = new Object();
    private boolean mIsAccShouldStop = true;

    public KikaGoAccessoryDataSource(Context context, UsbAccessory accessory) {
        mContext = context.getApplicationContext();
        mUsbAccessory = accessory;
    }

    @Override
    public boolean openUsb() {
        Logger.d("openUsb");
        try {
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            if (manager == null) {
                return false;
            }
            mParcelFileDescriptor = manager.openAccessory(mUsbAccessory);
            Logger.d("openUsb success = " + (mParcelFileDescriptor != null));
            return mParcelFileDescriptor != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void closeUsb() {
        Logger.d("closeUsb");
        try {
            if (mParcelFileDescriptor != null) {
                mParcelFileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mParcelFileDescriptor = null;
    }

    @Override
    public boolean open() {
        if (mParcelFileDescriptor != null) {
            FileDescriptor fd = mParcelFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            ReadThread readThread = new ReadThread(mInputStream);
            readThread.start();
        }
        return true;
    }

    @Override
    public void start() {
        synchronized (mSyncObj) {
            mIsAccShouldStop = false;
            mSyncObj.notify();
        }
    }

    @Override
    public void stop() {
        mIsAccShouldStop = true;
    }

    @Override
    public void close() {
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        synchronized (mSyncObj) {
            mInputStream = null;
            mSyncObj.notify();
        }
    }

    @Override
    public int checkVolumeState() {
        return ERROR_VOLUME_FW_NOT_SUPPORT;
    }

    @Override
    public int volumeUp() {
        return ERROR_VOLUME_FW_NOT_SUPPORT;
    }

    @Override
    public int volumeDown() {
        return ERROR_VOLUME_FW_NOT_SUPPORT;
    }

    @Override
    public byte[] checkFwVersion() {
        return new byte[] {0x00, 0x7F};
    }

    @Override
    public byte[] checkDriverVersion() {
        return new byte[] {0x00, 0x7F};
    }

    @Override
    public void setOnDataListener(OnDataListener listener) {
        mListener = listener;
    }

    // TODO : check ghost thread.
    private class ReadThread extends Thread {
        private byte[] mUsbData = new byte[64];

        ReadThread(FileInputStream stream) {
            this.setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            while (mParcelFileDescriptor != null) {
                try {
                    synchronized (mSyncObj) {
                    if (mInputStream != null) {
                        int readSize = mInputStream.read(mUsbData, 0, 64);
                            if (readSize > 0) {
                                if (mIsAccShouldStop) {
                                    try {
                                        mSyncObj.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Logger.v("ReadThread readSize = " + readSize);
                                    if (mListener != null) {
                                        mListener.onData(mUsbData, readSize);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
