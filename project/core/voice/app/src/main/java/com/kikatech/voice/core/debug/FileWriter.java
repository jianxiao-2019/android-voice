package com.kikatech.voice.core.debug;

import android.text.TextUtils;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    public void onData(byte[] data) {
        writeToFile(data);
        if (mDataOut != null) {
            mDataOut.onData(data);
        }
    }

    private void writeToFile(byte[] data) {
        // TODO : should write the data to file in the other thread.
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
