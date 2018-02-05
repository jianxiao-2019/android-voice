package com.kikatech.go.dialogflow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.kikatech.go.dialogflow.apiai.ApiAiAgentCreator;
import com.kikatech.go.util.FlavorUtil;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.util.request.RequestManager;
import com.xiao.usbaudio.AudioPlayBack;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowConfig {

    private static final String APP_NAME = "KikaGo";

    private static final int BOS_DURATION = 6800;

    public static VoiceConfiguration getVoiceConfig(Context ctx, UsbAudioSource audioSource) {
        String WEB_SOCKET_URL_DEV = FlavorUtil.isFlavorMain() ? VoiceConfiguration.HostUrl.DEV_MVP : VoiceConfiguration.HostUrl.DEV_KIKA;

        String debugFilePath = getDebugFilePath(ctx);
        AudioPlayBack.sFilePath = debugFilePath;
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.agent(new ApiAiAgentCreator())
                .source(audioSource);
        conf.setDebugFilePath(debugFilePath);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName(APP_NAME)
                .setUrl(WEB_SOCKET_URL_DEV)
                .setSign(RequestManager.getSign(ctx))
                .setUserAgent(RequestManager.generateUserAgent(ctx))
                .setAsrConfiguration(AsrConfigUtil.getConfig(AsrConfigUtil.ASRMode.ASR_MODE_DEFAULT))
                .build());
        conf.setBosDuration(BOS_DURATION);
        conf.setSupportWakeUpMode(true);
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

    private static void createFolderIfNecessary(File folder) {
        if (folder != null) {
            if (!folder.exists() || !folder.isDirectory()) {
                folder.mkdirs();
            }
        }
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
}