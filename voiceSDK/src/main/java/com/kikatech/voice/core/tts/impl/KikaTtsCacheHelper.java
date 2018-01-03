package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.text.TextUtils;

import com.kikatech.voice.util.log.FileLoggerUtil;
import com.kikatech.voice.util.log.LogUtil;
import com.kikatech.voice.util.request.MD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by brad_chang on 2017/12/15.
 */

public class KikaTtsCacheHelper {

    private static final String TAG = "KikaTtsSourceCache";

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
                    LogUtil.log(TAG, "sAssetsCacheList:" + sAssetsCacheList.get(i));
                }
            }
        }
    }

    static class CacheInfo {
        String speechText;
        String speechTextMd5;
        private String cacheFileName;

        CacheInfo(String jsonString) {
            try {
                JSONObject jo = new JSONObject(jsonString);
                speechText = jo.getJSONArray("contents").getJSONObject(0).getString("text");
            } catch (JSONException e) {
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
                LogUtil.log(TAG, "cache File:" + cacheFile.getAbsolutePath() + ", exist:" + cacheFile.exists());
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
            } catch (JSONException ignored) {

            }
            return jsonString;
        }
    }

    public static class TaskInfo {
        String downloadUrl;
        final CacheInfo cacheInfo;

        public TaskInfo(String ttsUrl, String jsonString) {
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

    private static void writeCacheList(CacheInfo ci) {
        if (sFileId == -1) {
            sFileId = FileLoggerUtil.getIns().configFileLogger(CACHE_FOLDER_PATH, CACHE_LIST_FILE, true);
        }
        FileLoggerUtil.getIns().writeLogToFile(sFileId, ci.getLogInfo(), true);
    }

    public static boolean downloadWithTask(TaskInfo ti) {
        if(ti == null) {
            return false;
        }
        if (ti.cacheInfo.hasFileCache()) {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "Cache hit :" + ti.cacheInfo.speechText + ", return");
            return true;
        } else {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "downloadFile ... voiceUrl:" + ti.downloadUrl + ", speechText:" + ti.cacheInfo.speechText);

            long t = System.currentTimeMillis();
            boolean ret = KikaTtsServerHelper.downloadFile(ti);
            if(ret) {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "Download complete, textQuery:" + ti.cacheInfo.speechText + ", hash:" + ti.cacheInfo.speechTextMd5);
                    writeCacheList(ti.cacheInfo);
                }
            }

            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "downloadFile ret:" + ret + ", spend:" + (System.currentTimeMillis() - t) + " ms");

            return ret;
        }
    }

    static String composeUrlVoiceSource(String ttsUrl) {
        String jsonString = "";
        try {
            JSONArray array = new JSONArray(ttsUrl);
            String url = array.getString(0);
            JSONObject json = new JSONObject();
            json.put("url", url);
            jsonString = json.toString();
        } catch (JSONException ignored) {
        }
        return jsonString;
    }

    static class MediaSource {
        private String assetFile = null;
        private String path = null;

        MediaSource(JSONObject json) {
            if (json.has(KEY_ASSET_FILE)) {
                try {
                    assetFile = json.getString(KEY_ASSET_FILE);
                } catch (JSONException ignored) {
                }
            } else if (json.has(KEY_URL)) {
                try {
                    path = json.getString(KEY_URL);
                } catch (JSONException ignored) {
                }
            } else if (json.has(KEY_FILE_CACHE)) {
                try {
                    path = json.getString(KEY_FILE_CACHE);
                } catch (JSONException ignored) {
                }
            }

            if (LogUtil.DEBUG && assetFile == null && path == null) {
                LogUtil.log(TAG, "Err, parse error, json:" + json.toString());
            }
        }

        AssetFileDescriptor getAssetFileDescriptor(Context ctx) {
            AssetFileDescriptor afd = null;
            if (!TextUtils.isEmpty(assetFile)) {
                try {
                    afd = ctx.getAssets().openFd(CACHE_FOLDER_NAME + "/" + assetFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return afd;
        }

        String getPathSource() {
            return path;
        }
    }

    static List<KikaTtsCacheHelper.MediaSource> parseMediaSource(String jsonString) {
        List<KikaTtsCacheHelper.MediaSource> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(jsonString);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jo = new JSONObject(arr.getString(i));
                list.add(new MediaSource(jo));
            }
        } catch (JSONException e) {
            // Not a Json array
            try {
                JSONObject jo = new JSONObject(jsonString);
                list.add(new MediaSource(jo));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        return list;
    }

    static String getCacheListJsonString(String jsonString) throws JSONException {
        JSONArray arr = new JSONObject(jsonString).getJSONArray("contents");
        JSONArray arrResult = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            String speechText = arr.getJSONObject(i).getString("text");
            CacheInfo ci = new CacheInfo(speechText);
            if (!ci.hasCache()) {
                return null;
            }
            String cacheJson = ci.getCacheJsonString();
            arrResult.put(cacheJson);
        }
        return arrResult.toString();
    }
}