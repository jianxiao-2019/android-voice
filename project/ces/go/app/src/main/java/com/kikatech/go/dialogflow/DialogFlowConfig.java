package com.kikatech.go.dialogflow;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.apiai.ApiAiAgentCreator;
import com.kikatech.go.dialogflow.apiai.TutorialAgentCreator;
import com.kikatech.go.services.presenter.KikaGoUsbVoiceSourceWrapper;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.HttpClient.HttpClientExecutor;
import com.kikatech.go.util.HttpClient.HttpClientTask;
import com.kikatech.go.util.HttpClient.HttpClientUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.google.cloud.speech.GoogleAesUtil;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.request.RequestManager;
import com.kikatech.voice.wakeup.SnowBoyDetector;
import com.kikatech.voice.webservice.google_cloud_speech.GoogleApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowConfig {
    private static final String TAG = "DialogFlowConfig";

    private static final String APP_NAME = "KikaGo";

    private static final int BOS_DURATION = 6800;
    private static final int EOS_DURATION = 3000;

    public static final int BOS_DURATION_TUTORIAL = 15000;

    private static final String API_GOOGLE_AUTH_JSON_URL = "http://api-dev.kika.ai/v3/auth/getGoogleKey";

    private static final boolean DEBUG_AUTH = false;


    public static void getVoiceConfig(Context ctx, KikaGoUsbVoiceSourceWrapper audioSource, final IConfigListener listener) {
        final VoiceConfiguration configuration = __getVoiceConfig(ctx, audioSource);
        __getGoogleAuthFile(ctx, new IGoogleAuthFileListener() {
            @Override
            public void onLoaded(String json) {
                if (!TextUtils.isEmpty(json)) {
                    configuration.setWebSocket(new GoogleApi(json));
                }
                dispatchConfiguration(listener, configuration);
            }
        });
    }

    public static void getTutorialConfig(Context ctx, KikaGoUsbVoiceSourceWrapper audioSource, final IConfigListener listener) {
        final VoiceConfiguration configuration = __getTutorialConfig(ctx, audioSource);
        __getGoogleAuthFile(ctx, new IGoogleAuthFileListener() {
            @Override
            public void onLoaded(String json) {
                if (!TextUtils.isEmpty(json)) {
                    configuration.setWebSocket(new GoogleApi(json));
                }
                dispatchConfiguration(listener, configuration);
            }
        });
    }

    private static VoiceConfiguration __getVoiceConfig(Context ctx, KikaGoUsbVoiceSourceWrapper audioSource) {
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
        conf.setSpeechMode(VoiceConfiguration.SpeechMode.ONE_SHOT);
        conf.setBosDuration(BOS_DURATION);
        conf.setEosDuration(EOS_DURATION);
        conf.setWakeUpDetector(new SnowBoyDetector(ctx));
        return conf;
    }

    private static VoiceConfiguration __getTutorialConfig(final Context ctx, final KikaGoUsbVoiceSourceWrapper audioSource) {
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
        conf.setSpeechMode(VoiceConfiguration.SpeechMode.ONE_SHOT);
        conf.setBosDuration(BOS_DURATION_TUTORIAL);
        conf.setEosDuration(EOS_DURATION);
        conf.setWakeUpDetector(new SnowBoyDetector(ctx));
        return conf;
    }

    private static void __getGoogleAuthFile(final Context context, @NonNull final IGoogleAuthFileListener listener) {
        if (DEBUG_AUTH) {
            BackgroundThread.post(new Runnable() {
                @Override
                public void run() {
                    listener.onLoaded(__getGoogleSpeechAuthJsonFromRaw(context));
                }
            });
        } else {
            HttpClientExecutor.getIns().asyncGET(API_GOOGLE_AUTH_JSON_URL, false, new HttpClientTask.HttpClientCallback() {
                @Override
                public void onResponse(Bundle result) {
                    if (LogUtil.DEBUG) {
                        LogUtil.logw(TAG, "onResponse");
                    }
                    String json = __getGoogleSpeechAuthJsonFromHttpResult(result);
                    listener.onLoaded(json);
                }

                @Override
                public void onError(String error) {
                    if (LogUtil.DEBUG) {
                        LogUtil.logw(TAG, String.format("onError, error: %s", error));
                    }
                    listener.onLoaded(null);
                }
            });
        }
    }

    private static String __getGoogleSpeechAuthJsonFromRaw(Context context) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "__getGoogleSpeechAuthJsonFromRaw");
        }
        InputStream is = context.getResources().openRawResource(R.raw.google_speech);
//        InputStream is = context.getResources().openRawResource(R.raw.google_speech_0);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return writer.toString();
    }

    private static String __getGoogleSpeechAuthJsonFromHttpResult(Bundle result) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "__getGoogleSpeechAuthJsonFromHttpResult");
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

    public interface IGoogleAuthFileListener {
        void onLoaded(String json);
    }

    public interface IConfigListener {
        void onDone(VoiceConfiguration config);
    }
}