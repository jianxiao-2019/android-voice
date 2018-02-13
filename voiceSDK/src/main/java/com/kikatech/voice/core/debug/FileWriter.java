package com.kikatech.voice.core.debug;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.FileLoggerUtil;
import com.kikatech.voice.util.log.Logger;

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
        mFilePath = DebugUtil.getDebugFilePath();
        if (mFilePath != null) {
            mFilePath += mSuffix;
        }
    }

    @Override
    public void onData(final byte[] data) {
        if(mFilePath != null) {
            FileLoggerUtil.getIns().writeToFile(data, mFilePath);
        }

        if (mNextPath != null) {
//            Logger.d("FileWriter pass data to next : " + mDataOut);
            mNextPath.onData(data);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " " + mSuffix;
    }
}
