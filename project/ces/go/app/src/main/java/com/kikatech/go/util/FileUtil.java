package com.kikatech.go.util;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author SkeeterWang Created on 2017/10/25.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileUtil {
    private static final String TAG = "FileUtil";

    private static final String RECORD_FOLDER = "/kika_go";
    private static final String EXTENSION_PNG = ".png";
    private static final String FILE_NAME = "kika_%s" + EXTENSION_PNG;

    public static String saveInPNG(Bitmap bitmap, String filePath) {
        /** Saving image file with appropriate compressing quality **/
        try {
            FileOutputStream out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            if (LogUtil.DEBUG) {
                File file = new File(filePath);
                FileInputStream is = new FileInputStream(file);
                byte[] data = getBytesFromStream(is);
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "final file image size: " + data.length / 1024 + " kb.");
                }
            }
            return filePath;
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    private static byte[] getBytesFromStream(InputStream is) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = is.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static String getImAvatarFilePath(String appName, String name) {
        File file = new File(getImRecordFolder(appName), String.format(FILE_NAME, name));
        return file.getAbsolutePath();
    }

    private static String getImRecordFolder(String appName) {
        String filePath = getRootRecordFolder();
        File file = new File(filePath, appName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    private static String getRootRecordFolder() {
        String folder = Environment.getExternalStorageDirectory().getPath();
        File file = new File(folder, RECORD_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }
}