package com.kikatech.go.dialogflow;

import android.content.Context;

import com.kikatech.go.dialogflow.apiai.ApiAiAgentCreator;
import com.kikatech.usb.UsbAudioSource;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.util.request.RequestManager;

/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowConfig {

    private static final String APP_NAME = "KikaGo";

    private static final int BOS_DURATION = 6800;
    private static final int EOS_DURATION = 3000;

    public static VoiceConfiguration getVoiceConfig(Context ctx, UsbAudioSource audioSource) {
        String WEB_SOCKET_URL_DEV = VoiceConfiguration.HostUrl.KIKA_GO;

        VoiceConfiguration conf = new VoiceConfiguration();
        conf.agent(new ApiAiAgentCreator())
                .source(audioSource);
        conf.setDebugFileTag(APP_NAME);
        conf.setIsDebugMode(true);
        conf.setConnectionConfiguration(new VoiceConfiguration.ConnectionConfiguration.Builder()
                .setAppName(APP_NAME)
                .setUrl(WEB_SOCKET_URL_DEV)
                .setSign(RequestManager.getSign(ctx))
                .setUserAgent(RequestManager.generateUserAgent(ctx))
                .setAsrConfiguration(AsrConfigUtil.getConfig(AsrConfigUtil.ASRMode.ASR_MODE_DEFAULT))
                .build());
        conf.setBosDuration(BOS_DURATION);
        conf.setEosDuration(EOS_DURATION);
        conf.setSupportWakeUpMode(true);
        return conf;
    }
}