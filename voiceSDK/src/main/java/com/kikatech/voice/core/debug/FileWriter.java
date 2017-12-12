package com.kikatech.voice.core.debug;

import android.text.TextUtils;
import android.util.Log;

import com.kikatech.voice.core.framework.IDataPath;
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

    private static ExecutorService sExecutor = null;

    public FileWriter(String filePath, IDataPath dataOut) {
        mFilePath = filePath;
        mDataOut = dataOut;

        if (Logger.DEBUG) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void onData(final byte[] data) {
        if (Logger.DEBUG && sExecutor != null) {
            sExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    writeToFile(data);
                }
            });
        }

        if (mDataOut != null) {
//            Logger.d("FileWriter pass data to next : " + mDataOut);
            mDataOut.onData(data);
        }
    }

    private void writeToFile(byte[] data) {
//        Logger.d("FileWriter writeToFile mFilePath = " + mFilePath + " data.length = " + data.length);
        if (TextUtils.isEmpty(mFilePath)) {
            return;
        }

        // Create file
        File file = new File(mFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        boolean append = true;
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            int len = data.length;
            os.write(data, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(os);
        }
    }

    private void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
