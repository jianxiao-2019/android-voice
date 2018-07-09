package com.kikatech.voice.core.debug;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.FileUtil;

import java.util.Arrays;

/**
 * Created by ryanlin on 25/12/2017.
 * Update by ryanlin on 25/12/2017.
 */

public class FileWriter extends IDataPath {
    private final String mSuffix;
    private String mFilePath;

    public FileWriter(String suffix, IDataPath dataPath) {
        super(dataPath);
        mSuffix = suffix;
    }

    @Override
    public void start() {
        super.start();
        mFilePath = DebugUtil.getAsrAudioFilePath();
        if (mFilePath != null) {
            mFilePath += mSuffix;
        }
    }

    @Override
    public void onData(byte[] data, int length) {
        if (mFilePath != null) {
            if (data.length != length) {
                data = Arrays.copyOf(data, length);
            }
            FileUtil.writeByteToFile(data, mFilePath);
        }

        if (mNextPath != null) {
            mNextPath.onData(data, length);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " " + mSuffix;
    }
}
