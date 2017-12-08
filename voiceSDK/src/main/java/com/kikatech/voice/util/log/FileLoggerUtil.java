package com.kikatech.voice.util.log;

import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import com.kikatech.voice.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by brad_chang on 2017/12/8.
 */

public class FileLoggerUtil {

    private final ExecutorService mExecutor;
    private final List<BufferedWriter> mBufferedWriterPool = new ArrayList<>();

    private final long mInitedTime = System.currentTimeMillis();

    private static FileLoggerUtil sFileLoggerUtil;

    private FileLoggerUtil() {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    public synchronized static FileLoggerUtil getIns() {
        if(sFileLoggerUtil == null) {
            sFileLoggerUtil = new FileLoggerUtil();
        }
        return sFileLoggerUtil;
    }

    public synchronized int configFileLogger(String logFolderPath, String logFilePath) {
        BufferedWriter logger = initLogger(logFolderPath, logFilePath);
        mBufferedWriterPool.add(logger);

        int idx = mBufferedWriterPool.indexOf(logger);
        writeBasicInfo(idx);

        return idx;
    }

    public File getLogFullPath(String logFolderPath, String logFilePath) {
        String fullFolderPath = createSDCardFolder(logFolderPath);
        String filename = String.format(logFilePath, DateFormat.format("yyyyMMdd_HHmmss", mInitedTime).toString());
        return new File(fullFolderPath + "/" + filename);
    }

    public String getDisplayInitTime() {
        return DateFormat.format("yyyy/MM/dd HH:mm:ss", mInitedTime).toString();
    }

    private void writeBasicInfo(int idx) {
        writeLogToFile(idx, getDisplayInitTime() + ", timestamp : " + System.currentTimeMillis());
        writeLogToFile(idx, "Voice SDK Version : " + BuildConfig.VERSION_NAME + " / " + BuildConfig.VERSION_CODE);
    }

    private BufferedWriter initLogger(String logFolderPath, String logFilePath) {
        File logFile = getLogFullPath(logFolderPath, logFilePath);
        if (!logFile.exists()) {
            try {
                boolean ret = logFile.createNewFile();
                Log.d("LogOnViewUtil", "Write logs to " + logFile + ", file status:" + ret);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return new BufferedWriter(new FileWriter(logFile, true));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeLogToFile(final int id, final String log) {
        if (mExecutor == null) {
            return;
        }
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedWriter logger = mBufferedWriterPool.get(id);
                    if (logger != null) {
                        String currentTime = DateFormat.format("MM/dd HH:mm:ss", System.currentTimeMillis()).toString();
                        logger.append("[").append(currentTime).append("] ").append(log).append("\n");
                        logger.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void exit() {
        for(BufferedWriter bw : mBufferedWriterPool) {
            if(bw != null) {
                try {
                    bw.close();
                    bw = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mBufferedWriterPool.clear();

        if(mExecutor != null) {
            mExecutor.shutdown();
        }
    }

    public String loadLogFile(String logFolderPath, String logFilePath) {
        File fullPath = getLogFullPath(logFolderPath, logFilePath);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fullPath));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

    private static String createSDCardFolder(String folder) {
        String sdcard = Environment.getExternalStorageDirectory().getPath();
        File file = new File(sdcard, folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }
}