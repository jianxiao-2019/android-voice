package com.kikatech.usb.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

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
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
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
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("file absolute path: %s", file.getAbsolutePath()));
            long lastModified = file.lastModified();
            LogUtil.logd(TAG, String.format("file last modified: %s", lastModified));
        }
    }


    private static boolean mkdirs(@NonNull File folder) {
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    private static boolean createNewFile(@NonNull File file) {
        try {
            return file.createNewFile();
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
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
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
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
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }
}