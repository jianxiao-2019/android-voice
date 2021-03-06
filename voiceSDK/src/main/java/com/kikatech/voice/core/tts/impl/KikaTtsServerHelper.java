package com.kikatech.voice.core.tts.impl;

import android.content.Context;

import com.kikatech.voice.util.CustomConfig;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by brad_chang on 2017/12/26.
 */

class KikaTtsServerHelper {

    private static final String TAG = "KikaTtsServerHelper";

    private static final String WEB_SOCKET_URL_DEV = "http://speech0-dev-mvp.kikakeyboard.com/v3/tts/sign";

    static String fetchTssUrl(Context context, String jsonString) {
        final long start_t = System.currentTimeMillis();

        final StringBuilder ttsUrl = new StringBuilder();
        BufferedReader in = null;
        try {
            URLConnection conn = new URL(WEB_SOCKET_URL_DEV + "?sign=" + RequestManager.getSign(context)).openConnection();

            int timeOut = CustomConfig.getKikaTtsServerTimeout();
            if (Logger.DEBUG)
                Logger.i(TAG, "timeOut : " + timeOut + " ms");
            conn.setConnectTimeout(timeOut);
            conn.setReadTimeout(timeOut);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("User-Agent", RequestManager.generateUserAgent(context));
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(jsonString);
            wr.flush();

            InputStream stream = conn.getInputStream();
            in = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = in.readLine()) != null) {
                ttsUrl.append(line).append("\n");
            }

        } catch (Exception e) {
            if (Logger.DEBUG)
                Logger.w(TAG, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (Logger.DEBUG)
            Logger.i(TAG, "Get Url end, spend:" + (System.currentTimeMillis() - start_t) + " ms, " + " ttsUrl = " + ttsUrl);

        return ttsUrl.toString();
    }

    static boolean downloadFile(KikaTtsCacheHelper.TaskInfo ti) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        int timeOut = CustomConfig.getKikaTtsServerTimeout();

        if (Logger.DEBUG) {
            Logger.i(TAG, "textMd5 : " + ti.cacheInfo.speechTextMd5 + ", textQuery:" + ti.cacheInfo.speechText + ", strUrl:" + ti.downloadUrl);
            Logger.i(TAG, "timeOut : " + timeOut + " ms");
        }
        try {
            URL url = new URL(ti.downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                if (Logger.DEBUG)
                    Logger.i(TAG, "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
                return false;
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
                if (Logger.DEBUG && fileLength > 0) {// only if total length is known
                    Logger.i(TAG, "progress:" + (int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }

            return true;

        } catch (Exception e) {
            if (Logger.DEBUG)
                Logger.i(TAG, "Err:" + e);
            return false;
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
    }
}