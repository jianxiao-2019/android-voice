package com.kikatech.voice.core.debug;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.FileLoggerUtil;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 25/12/2017.
 * Update by ryanlin on 25/12/2017.
 */

public class FileWriter extends IDataPath {

    private final String mFilePath;

    public FileWriter(String filePath, IDataPath dataPath) {
        super(dataPath);
        mFilePath = filePath;
    }

    @Override
    public void onData(final byte[] data) {
        if(Logger.DEBUG) {
            FileLoggerUtil.getIns().writeToFile(data, mFilePath);
        }

        if (mNextPath != null) {
//            Logger.d("FileWriter pass data to next : " + mDataOut);
            mNextPath.onData(data);
        }
    }

    @Override
    public String toString() {
        return super.toString() + mFilePath;
    }
}
