package com.kikatech.go.dialogflow;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.apiai.ApiAiAgentCreator;
import com.kikatech.go.dialogflow.apiai.TutorialAgentCreator;
import com.kikatech.go.util.HttpClient.HttpClientExecutor;
import com.kikatech.go.util.HttpClient.HttpClientTask;
import com.kikatech.go.util.HttpClient.HttpClientUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.google.cloud.speech.GoogleAesUtil;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.voice.core.webservice.impl.GoogleApi;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voice.wakeup.SnowBoyDetector;

/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowConfig {
    private static final String TAG = "DialogFlowConfig";

    private static final String APP_NAME = "KikaGo";

    private static final long FILE_ALIVE_DAYS = LogUtil.DEBUG ? -1 : 7;

    private static final int BOS_DURATION = 6800;
    private static final int EOS_DURATION = 3000;

    public static final int BOS_DURATION_TUTORIAL = 15000;

    private static final String API_GOOGLE_AUTH_JSON_URL = "http://api-dev.kika.ai/v3/auth/getGoogleKey";

    public static void getVoiceConfig(Context ctx, KikaGoVoiceSource audioSource, final IConfigListener listener) {
        final VoiceConfiguration configuration = __getVoiceConfig(ctx, audioSource);
        HttpClientExecutor.getIns().asyncGET(API_GOOGLE_AUTH_JSON_URL, false, new HttpClientTask.HttpClientCallback() {
            @Override
            public void onResponse(Bundle result) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onResponse");
                }
                String json = getGoogleSpeechAuthJsonFromHttpResult(result);
                if (!TextUtils.isEmpty(json)) {
                    configuration.setWebSocket(new GoogleApi(json));
                }
                dispatchConfiguration(listener, configuration);
            }

            @Override
            public void onError(String error) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, String.format("onError, error: %s", error));
                }
                dispatchConfiguration(listener, configuration);
            }
        });
    }

    public static void getTutorialConfig(Context ctx, KikaGoVoiceSource audioSource, final IConfigListener listener) {
        final VoiceConfiguration configuration = __getTutorialConfig(ctx, audioSource);
        HttpClientExecutor.getIns().asyncGET(API_GOOGLE_AUTH_JSON_URL, false, new HttpClientTask.HttpClientCallback() {
            @Override
            public void onResponse(Bundle result) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "onResponse");
                }
                String json = getGoogleSpeechAuthJsonFromHttpResult(result);
                if (!TextUtils.isEmpty(json)) {
                    configuration.setWebSocket(new GoogleApi(json));
                }
                dispatchConfiguration(listener, configuration);
            }

            @Override
            public void onError(String error) {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, String.format("onError, error: %s", error));
                }
                dispatchConfiguration(listener, configuration);
            }
        });
    }

    private static VoiceConfiguration __getVoiceConfig(Context ctx, KikaGoVoiceSource audioSource) {
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
        conf.setWakeUpDetector(new SnowBoyDetector(ctx));
        return conf;
    }

    private static VoiceConfiguration __getTutorialConfig(final Context ctx, final KikaGoVoiceSource audioSource) {
        VoiceConfiguration conf = new VoiceConfiguration();
        conf.agent(new TutorialAgentCreator())
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
        conf.setBosDuration(BOS_DURATION_TUTORIAL);
        conf.setEosDuration(EOS_DURATION);
        conf.setWakeUpDetector(new SnowBoyDetector(ctx));
        return conf;
    }

    private static String getGoogleSpeechAuthJsonFromHttpResult(Bundle result) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "getGoogleSpeechAuthJsonFromHttpResult");
        }
        if (result == null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "invalid result");
            }
            return null;
        }
        String encodedString = result.getString(HttpClientUtil.KEY_RESULT, null);
        if (TextUtils.isEmpty(encodedString)) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "invalid encodedString");
            }
            return null;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("encodedString: %s", encodedString));
        }
        String decodedString = GoogleAesUtil.decrypt(encodedString);
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("decodedString: %s", decodedString));
        }
        return decodedString;
    }

    private static void dispatchConfiguration(IConfigListener listener, VoiceConfiguration config) {
        if (listener != null) {
            listener.onDone(config);
        }
    }

    public interface IConfigListener {
        void onDone(VoiceConfiguration config);
    }
}