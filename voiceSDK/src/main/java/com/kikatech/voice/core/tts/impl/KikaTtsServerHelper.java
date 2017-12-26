package com.kikatech.voice.core.tts.impl;

import android.content.Context;
import android.os.Environment;

import com.kikatech.voice.util.log.LogUtil;
import com.kikatech.voice.util.request.RequestManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by brad_chang on 2017/12/26.
 */

class KikaTtsServerHelper {

    private static final String TAG = "KikaTtsServerHelper";

    private static final String WEB_SOCKET_URL_DEV = "http://speech0-dev-mvp.kikakeyboard.com/v3/tts/sign";
    private static final int TIMEOUT = 1500 * 2; // ConnectTimeout & ReadTimeout

    private static int getTimeout() {
        int timeout = TIMEOUT;
        try {
            timeout = __getTimeout();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return timeout / 2;
    }

    private static int __getTimeout() throws JSONException, IOException {
        int timeout = TIMEOUT;
        if (LogUtil.DEBUG) {
            String folder = Environment.getExternalStorageDirectory().getPath();
            File file = new File(folder, "kika_go");
            File config = new File(file, "tts_server_config.txt");
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "config file :" + config);
            if (!config.exists()) {
                JSONObject json = new JSONObject();
                json.put("timeout", TIMEOUT);
                PrintWriter out = null;
                try {
                    out = new PrintWriter(config);
                    out.println(json.toString());
                } finally {
                    if (out != null) out.close();
                }
                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "write tts_server_config ok");

                return TIMEOUT;
            } else {
                try (BufferedReader br = new BufferedReader(new FileReader(config.getAbsolutePath()))) {
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                        line = br.readLine();
                    }
                    String json = sb.toString();
                    JSONObject jsonConfig = new JSONObject(json);
                    timeout = jsonConfig.getInt("timeout");
                    if (LogUtil.DEBUG)
                        LogUtil.log(TAG, "tts_server_config :" + jsonConfig);
                }
            }
        }
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "Kika Tts Server connection timeout : " + timeout);
        return timeout;
    }

    static String fetchTssUrl(Context context, String jsonString) {
        final long start_t = System.currentTimeMillis();

        final StringBuilder ttsUrl = new StringBuilder();
        BufferedReader in = null;
        try {
            URLConnection conn = new URL(WEB_SOCKET_URL_DEV + "?sign=" + RequestManager.getSign(context)).openConnection();

            int timeOut = getTimeout();
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "timeOut : " + timeOut + " ms");
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
            if (LogUtil.DEBUG)
                LogUtil.logw(TAG, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (LogUtil.DEBUG)
            LogUtil.log(TAG, " Get Url end, spend:" + (System.currentTimeMillis() - start_t) + " ms, " + " ttsUrl = " + ttsUrl);

        return ttsUrl.toString();
    }

    static boolean downloadFile(KikaTtsCacheHelper.TaskInfo ti) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        int timeOut = getTimeout();

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "textMd5:" + ti.cacheInfo.speechTextMd5 + ", textQuery:" + ti.cacheInfo.speechText + ", strUrl:" + ti.downloadUrl);
            LogUtil.log(TAG, "timeOut : " + timeOut + " ms");
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
                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
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
                if (LogUtil.DEBUG && fileLength > 0) {// only if total length is known
                    LogUtil.log(TAG, "progress:" + (int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }

            return true;

        } catch (Exception e) {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "Err:" + e);
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