package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.kikatech.voice.util.BackgroundThread;
import com.kikatech.voice.util.log.FileLoggerUtil;
import com.kikatech.voice.util.log.LogUtil;
import com.kikatech.voice.util.request.MD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by brad_chang on 2017/12/15.
 */

class KikaTtsCacheHelper {

    private final static String KEY_ASSET_FILE = "asset_file";
    private final static String KEY_URL = "url";
    private final static String KEY_FILE_CACHE = "file_cache";

    private final static String CACHE_FOLDER_NAME = "tts_cache";
    private final static String CACHE_FOLDER_PATH = "kika_go/" + CACHE_FOLDER_NAME;
    private final static String CACHE_LIST_FILE = "tts_cache.list";
    private static int sFileId = -1;

    private static List<String> sAssetsCacheList = null;

    static void init(Context ctx) {
        if (sAssetsCacheList == null) {
            String[] assetsCaches = null;
            try {
                assetsCaches = ctx.getAssets().list(CACHE_FOLDER_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (assetsCaches != null) {
                sAssetsCacheList = Arrays.asList(assetsCaches);
            }
            if (LogUtil.DEBUG) {
                for (int i = 0; i < sAssetsCacheList.size(); i++) {
                    LogUtil.log("KikaTtsSource", "sAssetsCacheList:" + sAssetsCacheList.get(i));
                }
            }
        }
    }

    static class CacheInfo {
        private String speechText;
        private String speechTextMd5;
        private String cacheFileName;

        CacheInfo(String jsonString) {
            try {
                JSONObject jo = new JSONObject(jsonString);
                speechText = jo.getJSONArray("contents").getJSONObject(0).getString("text");
            } catch (JSONException e) {
                e.printStackTrace();
                speechText = jsonString;
            }
            speechTextMd5 = MD5.getMD5(speechText);
            cacheFileName = speechTextMd5 + ".mp3";
        }

        boolean hasCache() {
            return hasAssetsCache() || hasFileCache();
        }

        boolean hasFileCache() {
            return getFileCache().exists();
        }

        boolean hasAssetsCache() {
            return (sAssetsCacheList != null && sAssetsCacheList.contains(cacheFileName));
        }

        File getFileCache() {
            File cacheFile = FileLoggerUtil.getIns().getLogFullPath(CACHE_FOLDER_PATH, cacheFileName);
            if (LogUtil.DEBUG) {
                LogUtil.log("KikaTtsSource", "cache File:" + cacheFile.getAbsolutePath() + ", exist:" + cacheFile.exists());
            }
            return cacheFile;
        }

        String getLogInfo() {
            return speechTextMd5 + " " + speechText + "\n";
        }

        String getCacheJsonString() {
            String jsonString = "";
            try {
                JSONObject json = new JSONObject();
                if (hasAssetsCache()) {
                    json.put(KEY_ASSET_FILE, cacheFileName);
                } else {
                    File fileCache = getFileCache();
                    if (fileCache.exists()) {
                        json.put(KEY_FILE_CACHE, Uri.parse(fileCache.getAbsolutePath()).toString());
                    }
                }
                jsonString = json.toString();
            } catch (JSONException e) {

            }
            return jsonString;
        }
    }

    static class TaskInfo {
        private String downloadUrl;
        private final CacheInfo cacheInfo;

        TaskInfo(String ttsUrl, String jsonString) {
            cacheInfo = new CacheInfo(jsonString);

            JSONArray jourl;
            try {
                jourl = new JSONArray(ttsUrl);
                downloadUrl = jourl.getString(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static String downloadFile(TaskInfo ti) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        //String textMd5 = md5(textQuery);
        if (LogUtil.DEBUG)
            LogUtil.log("KikaTtsSource", "textMd5:" + ti.cacheInfo.speechTextMd5 + ", textQuery:" + ti.cacheInfo.speechText + ", strUrl:" + ti.downloadUrl);
        try {
            URL url = new URL(ti.downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            File saveFile = ti.cacheInfo.getFileCache();
            output = new FileOutputStream(saveFile);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                if (LogUtil.DEBUG && fileLength > 0) {// only if total length is known
                    LogUtil.log("KikaTtsSource", "progress:" + (int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }

            if (LogUtil.DEBUG) {
                LogUtil.log("KikaTtsSource", "Download complete, textQuery:" + ti.cacheInfo.speechText + ", hash:" + ti.cacheInfo.speechTextMd5 + ", " + saveFile.getAbsolutePath());
                writeCacheList(ti.cacheInfo);
            }


        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    private static void writeCacheList(CacheInfo ci) {
        if (sFileId == -1) {
            sFileId = FileLoggerUtil.getIns().configFileLogger(CACHE_FOLDER_PATH, CACHE_LIST_FILE, true);
        }
        FileLoggerUtil.getIns().writeLogToFile(sFileId, ci.getLogInfo(), true);
    }

    private static void downloadWithUrl(TaskInfo ti) {
        if (ti.cacheInfo.hasFileCache()) {
            if (LogUtil.DEBUG)
                LogUtil.log("KikaTtsSource", "Cache hit :" + ti.cacheInfo.speechText + ", return");
        } else {
            if (LogUtil.DEBUG)
                LogUtil.log("KikaTtsSource", "downloadFile ... voiceUrl:" + ti.downloadUrl + ", speechText:" + ti.cacheInfo.speechText);

            long t = System.currentTimeMillis();
            String ret = downloadFile(ti);

            if (LogUtil.DEBUG)
                LogUtil.log("KikaTtsSource", "downloadFile ret:" + ret + ", spend:" + (System.currentTimeMillis() - t) + " ms");
        }
    }

    static void submitDownloadTask(final String ttsUrl, final String jsonString) {
        BackgroundThread.post(
                new Runnable() {
                    @Override
                    public void run() {
                        KikaTtsCacheHelper.TaskInfo task = new KikaTtsCacheHelper.TaskInfo(ttsUrl, jsonString);
                        downloadWithUrl(task);
                    }
                });
    }

    static String composeUrlVoiceSource(String ttsUrl) {
        String jsonString = "";
        try {
            JSONArray array = new JSONArray(ttsUrl);
            String url = array.getString(0);
            JSONObject json = new JSONObject();
            json.put("url", url);
            jsonString = json.toString();
        } catch (JSONException e) {

        }
        return jsonString;
    }

    static class MediaSource {
        private AssetFileDescriptor afd = null;
        private String path = null;

        MediaSource(Context ctx, String jsonString) throws JSONException, IOException {
            JSONObject json = new JSONObject(jsonString);
            if (json.has(KEY_ASSET_FILE)) {
                String assetFile = json.getString(KEY_ASSET_FILE);
                afd = ctx.getAssets().openFd(CACHE_FOLDER_NAME + "/" + assetFile);
                LogUtil.log("KikaTtsSource", "afd.toString():" + afd.toString());
            } else if (json.has(KEY_URL)) {
                path = json.getString(KEY_URL);
            } else if (json.has(KEY_FILE_CACHE)) {
                path = json.getString(KEY_FILE_CACHE);
            }
        }

        AssetFileDescriptor getAssetFileDescriptor() {
            return afd;
        }

        String getPathSource() {
            return path;
        }
    }
}