package com.kikatech.go.dialogflow;

import android.content.Context;

import com.kikatech.go.dialogflow.apiai.ApiAiAgentCreator;
import com.kikatech.go.util.LogUtil;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voice.wakeup.SnowBoyDetector;

/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowConfig {

    private static final String APP_NAME = "KikaGo";

    private static final long FILE_ALIVE_DAYS = LogUtil.DEBUG ? -1 : 7;

    private static final int BOS_DURATION = 6800;
    private static final int EOS_DURATION = 3000;

    public static VoiceConfiguration getVoiceConfig(Context ctx, KikaGoVoiceSource audioSource) {
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.agent(new ApiAiAgentCreator())
                .source(audioSource);
        conf.setDebugFileTag(APP_NAME);
        conf.setIsDebugMode(true);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName(APP_NAME)
                .setUrl(UserSettings.getDbgAsrServer())
                .setSign(RequestManager.getSign(ctx))
                .setUserAgent(RequestManager.generateUserAgent(ctx))
                .setAsrConfiguration(AsrConfigUtil.getConfig(AsrConfigUtil.ASRMode.ASR_MODE_DEFAULT))
                .build());
        conf.setExternalConfig(new VoiceConfiguration.ExternalConfig.Builder()
                .setDebugLogAliveDays(FILE_ALIVE_DAYS)
                .addFolderConfig(LogUtil.LOG_FOLDER, FILE_ALIVE_DAYS)
                .addFolderConfig(Logger.LOG_FOLDER, FILE_ALIVE_DAYS)
                .build());
        conf.setSpeechMode(VoiceConfiguration.SpeechMode.ONE_SHOT);
        conf.setBosDuration(BOS_DURATION);
        conf.setEosDuration(EOS_DURATION);
        conf.setWakeUpDetector(new SnowBoyDetector());
        return conf;
    }
}