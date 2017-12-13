package com.kikatech.voice.core.debug;

import android.text.TextUtils;
import android.util.Log;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.FileLoggerUtil;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            FileLoggerUtil.getIns().asyncWriteToFile(data, mFilePath);
        }

        if (mDataOut != null) {
//            Logger.d("FileWriter pass data to next : " + mDataOut);
            mDataOut.onData(data);
        }
    }
}
