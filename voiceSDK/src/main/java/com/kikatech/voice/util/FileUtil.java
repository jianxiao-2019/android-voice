package com.kikatech.voice.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.voice.util.log.Logger;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;

/**
 * @author SkeeterWang Created on 2018/6/28.
 */

public class FileUtil {
    private static final String TAG = "FileUtil";


    public static void writeByteToFile(byte[] data, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        // Create file
        File file = new File(filePath);
        if (!file.exists()) {
            boolean success = createNewFile(file);
            if (!success) {
                return;
            }
        }
        writeByteToFile(data, file);
    }

    public static void writeByteToFile(byte[] data, @NonNull File file) {
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            bos = new BufferedOutputStream(fos);
            bos.write(data, 0, data.length);
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        } finally {
            flushOS(bos);
            closeIO(bos);
        }
    }


    public static File[] listFiles(String path) {
        File dir = new File(path);
        return listFiles(dir);
    }

    public static File[] listFiles(File dir) {
        boolean isValidDir = dir != null && dir.exists() && dir.isDirectory();
        return isValidDir ? dir.listFiles() : null;
    }

    public static File getDir(String path) {
        File dir = new File(path);
        boolean success = mkdirs(dir);
        return success ? dir : null;
    }


    public static void printFile(String TAG, @NonNull File file) {
        if (Logger.DEBUG) {
            Logger.d(TAG, String.format("file absolute path: %s", file.getAbsolutePath()));
            long lastModified = file.lastModified();
            Logger.d(TAG, String.format("file last modified: %s", lastModified));
        }
    }


    private static boolean mkdirs(@NonNull File folder) {
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    private static boolean createNewFile(@NonNull File file) {
        try {
            return file.createNewFile();
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return false;
    }


    private static void flushOS(Flushable... flushables) {
        if (flushables == null) {
            return;
        }
        try {
            for (Flushable flushable : flushables) {
                if (flushable != null) {
                    flushable.flush();
                }
            }
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    private static void closeIO(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }
}