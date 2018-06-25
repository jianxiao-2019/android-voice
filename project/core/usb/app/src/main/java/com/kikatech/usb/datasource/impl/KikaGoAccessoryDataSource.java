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
import java.io.FileOutputStream;
import java.io.IOException;

import static com.kikatech.usb.datasource.KikaGoVoiceSource.ERROR_VOLUME_FW_NOT_SUPPORT;

/**
 * Created by ryanlin on 2018/5/9.
 */

public class KikaGoAccessoryDataSource implements IUsbAudioDriver, IUsbDataSource {
    private static final String TAG = "KikaGoAccessoryDataSource";

    private static final int LENGTH_AUDIO = 64;
    private static final int LENGTH_CMD = 32;

    private static final int RETRY_INTERVAL = 20;
    private static final int RETRY_MAX_TIMES = 50;

    private static final byte CMD_AUDIO = (byte) 0xFF;
    private static final byte CMD_VOLUME_UP = 0x24;
    private static final byte CMD_VOLUME_DOWN = 0x25;
    private static final byte CMD_CHECK_VOLUME_STATE = 0x26;
    private static final byte CMD_CHECK_VERSION = 0x28;

    private Context mContext;
    private UsbAccessory mUsbAccessory;

    private ParcelFileDescriptor mParcelFileDescriptor;
    private FileInputStream mInputStream = null;
    private FileOutputStream mOutputStream = null;

    private OnDataListener mListener;

    private final Object mSyncObj = new Object();
    private int volume;
    private byte[] fw_version = new byte[2];
    private boolean mIsAccShouldStop = true;
    private boolean isValidCmdVolume;
    private boolean isValidCmdVersion;

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
            mOutputStream = new FileOutputStream(fd);
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
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        synchronized (mSyncObj) {
            mInputStream = null;
            mOutputStream = null;
            mSyncObj.notify();
        }
    }

    @Override
    public synchronized int checkVolumeState() {
        int retry = 0;
        isValidCmdVolume = false;
        volume = ERROR_VOLUME_FW_NOT_SUPPORT;
        while (!isValidCmdVolume && retry++ < RETRY_MAX_TIMES) {
            sendCommand(genCommand(CMD_CHECK_VOLUME_STATE));
            try {
                Thread.sleep(RETRY_INTERVAL);
            } catch (Exception ignore) {
            }
        }
        return volume;
    }

    @Override
    public synchronized int volumeUp() {
        int current_volume = checkVolumeState();
        if (current_volume < 0 || current_volume > 9) {
            return ERROR_VOLUME_FW_NOT_SUPPORT;
        } else if (current_volume == 9) {
            return current_volume;
        }
        int new_volume = current_volume + 1;
        sendCommand(genCommand(CMD_VOLUME_UP, (byte) new_volume));
        try {
            Thread.sleep(RETRY_INTERVAL);
        } catch (Exception ignore) {
        }
        return checkVolumeState();
    }

    @Override
    public synchronized int volumeDown() {
        int current_volume = checkVolumeState();
        if (current_volume < 0 || current_volume > 9) {
            return ERROR_VOLUME_FW_NOT_SUPPORT;
        } else if (current_volume == 1) {
            return current_volume;
        }
        int new_volume = current_volume - 1;
        sendCommand(genCommand(CMD_VOLUME_DOWN, (byte) new_volume));
        try {
            Thread.sleep(RETRY_INTERVAL);
        } catch (Exception ignore) {
        }
        return checkVolumeState();
    }

    @Override
    public synchronized byte[] checkFwVersion() {
        int retry = 0;
        isValidCmdVersion = false;
        fw_version[0] = (byte) 0xFF;
        fw_version[1] = (byte) 0x7F;
        while (!isValidCmdVersion && retry++ < RETRY_MAX_TIMES) {
            sendCommand(genCommand(CMD_CHECK_VERSION));
            try {
                Thread.sleep(RETRY_INTERVAL);
            } catch (Exception ignore) {
            }
        }
        return fw_version;
    }

    @Override
    public byte[] checkDriverVersion() {
        return new byte[]{0x00, 0x7F};
    }

    @Override
    public void setOnDataListener(OnDataListener listener) {
        mListener = listener;
    }

    private byte[] genCommand(byte cmd) {
        return genCommand(cmd, null);
    }

    private byte[] genCommand(byte cmd, Byte payload) {
        byte[] cmdBuffer = new byte[LENGTH_CMD];
        cmdBuffer[0] = cmd;
        if (payload != null) {
            cmdBuffer[1] = payload;
        }
        return cmdBuffer;
    }

    private synchronized void sendCommand(byte[] cmdBuffer) {
        write(cmdBuffer, LENGTH_CMD);
    }

    /**
     * write data to output stream
     */
    private synchronized void write(byte[] data, int length) {
        if (mOutputStream == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid output stream");
            }
            return;
        }
        try {
            mOutputStream.write(data, 0, length);
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    // TODO : check ghost thread.
    private class ReadThread extends Thread {
        private byte[] mUsbData = new byte[LENGTH_AUDIO];

        ReadThread(FileInputStream stream) {
            this.setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            while (mParcelFileDescriptor != null) {
                try {
                    synchronized (mSyncObj) {
                        if (mInputStream != null) {
                            int readSize = mInputStream.read(mUsbData, 0, LENGTH_AUDIO);
                            if (readSize > 0) {
                                if (mIsAccShouldStop) {
                                    try {
                                        mSyncObj.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
//                                    Logger.v("ReadThread readSize = " + readSize);
                                    boolean isAudioData = analyse_data(mUsbData, readSize) == CMD_AUDIO;
                                    if (isAudioData) {
                                        if (mListener != null) {
                                            mListener.onData(mUsbData, readSize);
                                        }
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

        private byte analyse_data(byte[] data, int length) {
            if (length < 63) {
                return -1;
            }
            byte cmd = data[3];
            boolean isValidCmdResult;
            switch (cmd) {
                case CMD_CHECK_VOLUME_STATE:
                case CMD_VOLUME_UP:
                case CMD_VOLUME_DOWN:
                    isValidCmdResult = data[0] == 0x00 && data[1] == 0x00 && data[2] == 0x00;
                    if (isValidCmdResult) {
                        volume = (int) data[4];
                        isValidCmdVolume = true;
                        return cmd;
                    }
                case CMD_CHECK_VERSION:
                    isValidCmdResult = data[0] == 0x00 && data[1] == 0x00 && data[2] == 0x00;
                    if (isValidCmdResult) {
                        fw_version[0] = data[4];
                        fw_version[1] = data[5];
                        isValidCmdVersion = true;
                        return cmd;
                    }
                    break;
                default:
                    break;
            }
            return CMD_AUDIO;
        }
    }
}
