package com.kikatech.usb.util.debug;

import com.kikatech.usb.util.FileUtil;

import java.util.Arrays;

/**
 * Created by ryanlin on 25/12/2017.
 * Update by ryanlin on 25/12/2017.
 */

public class FileWriter {

    private final String mSuffix;
    private String mFilePath;

    public FileWriter(String suffix) {
        mSuffix = suffix;
    }

    public void start() {
        mFilePath = DebugUtil.getAudioFilePath();
        if (mFilePath != null) {
            mFilePath += mSuffix;
        }
    }

    public void onData(byte[] data, int length) {
        if (mFilePath != null) {
            if (data.length != length) {
                data = Arrays.copyOf(data, length);
            }
            FileUtil.writeByteToFile(data, mFilePath);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " " + mSuffix;
    }
}
