package com.kikatech.voice.util;

import android.os.Environment;
import android.text.TextUtils;

import com.kikatech.voice.util.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by brad_chang on 2017/12/27.
 */

public class CustomConfig {

    private static final String TAG = "CustomConfig";

    private static final String CONFIG_FOLDER = "kika_go";

    // Kika tts server timeout config
    private static final String CONFIG_FILE_TTS_SERVER = "tts_server_config.txt";
    private static final int DEFAULT_TTS_SERVER_TIMEOUT = 375 * 2; // ConnectTimeout & ReadTimeout
    private static final String TAG_TIMEOUT = "timeout";
    private static int TIMEOUT = -1;

    // Snowboy sensitivity config
    private static final String CONFIG_FILE_SNOWBOY = "snowboy_config.txt";
    private static final String TAG_SNOWBOY_SENSITIVITY = "sensitivity";
    private static final String DEFAULT_SNOWBOY_SENSITIVITY = "0.8";
    private static String SNOWBOY_SENSITIVITY = null;


    public static void removeAllCustomConfigFiles() {
        String[] files = new String[]{CONFIG_FILE_TTS_SERVER, CONFIG_FILE_SNOWBOY};
        for (String filename : files) {
            File f = getConfigFile(filename);
            if (f.exists()) {
                boolean ret = f.delete();
                if (Logger.DEBUG)
                    Logger.i(TAG, "Delete config file " + f.getAbsolutePath() + " : " + ret);
            }
        }
    }

    public static synchronized int getKikaTtsServerTimeout() {
        if (TIMEOUT == -1) {
            int timeout = DEFAULT_TTS_SERVER_TIMEOUT;
            try {
                timeout = __getTtsServerTimeout();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            TIMEOUT = timeout / 2;
        }
        if (Logger.DEBUG)
            Logger.i(TAG, "Kika Tts Server Timeout:" + TIMEOUT + ", default:" + (DEFAULT_TTS_SERVER_TIMEOUT / 2));
        return TIMEOUT;
    }

    private static File getConfigFile(String filename) {
        String folder = Environment.getExternalStorageDirectory().getPath();
        File file = new File(folder, CONFIG_FOLDER);
        return new File(file, filename);
    }

    public static String getSnowboySensitivity() {
        if (TextUtils.isEmpty(SNOWBOY_SENSITIVITY)) {
            try {
                SNOWBOY_SENSITIVITY = __getSensitivity();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                SNOWBOY_SENSITIVITY = DEFAULT_SNOWBOY_SENSITIVITY;
            }
        }
        if (Logger.DEBUG)
            Logger.i(TAG, "Snowboy Sensitivity:" + SNOWBOY_SENSITIVITY + ", default:" + DEFAULT_SNOWBOY_SENSITIVITY);
        return SNOWBOY_SENSITIVITY;
    }

    private static String __getSensitivity() throws JSONException, IOException {
        String sen = DEFAULT_SNOWBOY_SENSITIVITY;
        if (Logger.DEBUG) {
            File config = getConfigFile(CONFIG_FILE_SNOWBOY);
            if (Logger.DEBUG)
                Logger.i(TAG, "config file :" + config);
            if (!config.exists()) {
                JSONObject json = new JSONObject();
                json.put(TAG_SNOWBOY_SENSITIVITY, DEFAULT_SNOWBOY_SENSITIVITY);
                PrintWriter out = null;
                try {
                    out = new PrintWriter(config);
                    out.println(json.toString());
                } finally {
                    if (out != null) out.close();
                }
                if (Logger.DEBUG)
                    Logger.i(TAG, "write " + CONFIG_FILE_SNOWBOY + " ok");

                return DEFAULT_SNOWBOY_SENSITIVITY;
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
                    sen = jsonConfig.getString(TAG_SNOWBOY_SENSITIVITY);
                    if (Logger.DEBUG)
                        Logger.i(TAG, CONFIG_FILE_SNOWBOY + " :" + jsonConfig);
                }
            }
        }
        if (Logger.DEBUG)
            Logger.i(TAG, "Snowboy sensitivity : " + sen);
        return sen;
    }

    private static int __getTtsServerTimeout() throws JSONException, IOException {
        int timeout = DEFAULT_TTS_SERVER_TIMEOUT;
        if (Logger.DEBUG) {
            File config = getConfigFile(CONFIG_FILE_TTS_SERVER);
            if (Logger.DEBUG)
                Logger.i(TAG, "config file :" + config);
            if (!config.exists()) {
                JSONObject json = new JSONObject();
                json.put(TAG_TIMEOUT, DEFAULT_TTS_SERVER_TIMEOUT);
                PrintWriter out = null;
                try {
                    out = new PrintWriter(config);
                    out.println(json.toString());
                } finally {
                    if (out != null) out.close();
                }
                if (Logger.DEBUG)
                    Logger.i(TAG, "write " + CONFIG_FILE_TTS_SERVER + " ok");

                return DEFAULT_TTS_SERVER_TIMEOUT;
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
                    timeout = jsonConfig.getInt(TAG_TIMEOUT);
                    if (Logger.DEBUG)
                        Logger.i(TAG, CONFIG_FILE_TTS_SERVER + " :" + jsonConfig);
                }
            }
        }
        if (Logger.DEBUG)
            Logger.i(TAG, "Kika Tts Server connection timeout : " + timeout);
        return timeout;
    }
}