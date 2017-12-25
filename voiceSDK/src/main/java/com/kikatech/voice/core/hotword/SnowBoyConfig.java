package com.kikatech.voice.core.hotword;

import android.os.Environment;

import com.kikatech.voice.util.log.LogUtil;
import com.kikatech.voice.util.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by brad_chang on 2017/12/14.
 */

public class SnowBoyConfig {

    private static final String SENSITIVITY = "0.8";

    public static String getSensitivity() {
        try {
            return __getSensitivity();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return SENSITIVITY;
    }

    private static String __getSensitivity() throws JSONException, IOException {
        String sen;
        if (LogUtil.DEBUG) {
            String folder = Environment.getExternalStorageDirectory().getPath();
            File file = new File(folder, "kika_go");
            File config = new File(file, "snowboy_config.txt");
            Logger.d("[sboy] config file :" + config);
            if (!config.exists()) {
                JSONObject json = new JSONObject();
                json.put("sensitivity", SENSITIVITY);
                PrintWriter out = null;
                try {
                    out = new PrintWriter(config);
                    out.println(json.toString());
                } finally {
                    if (out != null) out.close();
                }
                Logger.d("[sboy] write config ok");

                return SENSITIVITY;
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
                    sen = jsonConfig.getString("sensitivity");
                    Logger.d("[sboy]jsonConfig :" + jsonConfig);
                }
            }
        }
        Logger.d("[sboy]SnowBoyDetector SENSITIVITY:" + sen);
        return sen;
    }
}