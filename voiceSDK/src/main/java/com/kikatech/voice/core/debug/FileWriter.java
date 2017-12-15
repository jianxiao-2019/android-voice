package com.kikatech.voice.core.debug;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.FileLoggerUtil;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 06/11/2017.
 */

public class FileWriter implements IDataPath {

    private final String mFilePath;
    private final IDataPath mDataOut;

    public FileWriter(String filePath, IDataPath dataOut) {
        mFilePath = filePath;
        mDataOut = dataOut;
    }

    @Override
    public void onData(final byte[] data) {
        if(Logger.DEBUG) {
            FileLoggerUtil.getIns().writeToFile(data, mFilePath);
        }

        if (mDataOut != null) {
//            Logger.d("FileWriter pass data to next : " + mDataOut);
            mDataOut.onData(data);
        }
    }
}
