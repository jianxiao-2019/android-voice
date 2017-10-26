package com.kikatech.usb.util;

import android.os.Environment;

import java.io.File;

/**
 * @author SkeeterWang Created on 2017/10/25.
 */
public class FileUtil
{
	private static final String TAG = "FileUtil";

	private static final String RECORD_FOLDER = "/kika";
	private static final String EXTENSION_PCM = ".pcm";
	private static final String FILE_NAME = "kika_%s" + EXTENSION_PCM;

	public static String getRecordFilePath()
	{
		File file = new File( getRecordFolder(), String.format( FILE_NAME, System.currentTimeMillis() ) );
		return file.getAbsolutePath();
	}

	private static String getRecordFolder()
	{
		String folder = Environment.getExternalStorageDirectory().getPath();
		File file = new File( folder, RECORD_FOLDER );
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath();
	}
}
