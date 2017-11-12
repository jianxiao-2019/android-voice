package com.kikatech.go.dialogflow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.kikatech.go.dialogflow.apiai.ApiAiAgentCreator;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.util.PreferenceUtil;
import com.kikatech.voice.util.request.RequestManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowConfig {

    public static VoiceConfiguration queryDemoConfig(Context ctx) {
        String WEB_SOCKET_URL_DEV = "ws://speech0-dev-mvp.kikakeyboard.com/v2/speech";

        Locale[] LOCALE_LIST = new Locale[] {
                new Locale("en", "US"),
                new Locale("zh", "CN"),
        };

        VoiceConfiguration conf = new VoiceConfiguration();
        conf.agent(new ApiAiAgentCreator());
        conf.setDebugFilePath(getDebugFilePath(ctx));
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setUrl(WEB_SOCKET_URL_DEV)
                .setLocale(getCurrentLocale(ctx, LOCALE_LIST))
                .setSign(RequestManager.getSign(ctx))
                .setUserAgent(RequestManager.generateUserAgent(ctx))
                .build());

        return conf;
    }

    private static String getDebugFilePath(Context context) {
        if (context == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("en"));
        Date resultDate = new Date(System.currentTimeMillis());
        String timeStr = sdf.format(resultDate);

        return getCacheDir(context).toString() + "/kika_voice_" + timeStr;
    }

    private static boolean createFolderIfNecessary(File folder) {
        if (folder != null) {
            if (!folder.exists() || !folder.isDirectory()) {
                return folder.mkdirs();
            }
            return true;
        }
        return false;
    }

    private static File getCacheDir(@NonNull Context context) {
        try {
            File[] files = ContextCompat.getExternalCacheDirs(context);
            if (files != null && files.length > 0) {
                File file = files[0];
                if (file != null) {
                    createFolderIfNecessary(file);
                    return file;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return context.getCacheDir();
    }

    private static String getCurrentLocale(Context ctx, Locale[] LOCALE_LIST) {
        int current = PreferenceUtil.getInt(ctx, PreferenceUtil.KEY_LANGUAGE, 0);
        if (current >= LOCALE_LIST.length) {
            return LOCALE_LIST[0].toString();
        }
        return LOCALE_LIST[current].toString();
    }
}
