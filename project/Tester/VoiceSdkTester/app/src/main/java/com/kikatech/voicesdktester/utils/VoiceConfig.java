package com.kikatech.voicesdktester.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.BackgroundThread;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voice.webservice.google_cloud_speech.GoogleApi;
import com.kikatech.voice.webservice.tencent_cloud_speech.TencentApi;
import com.kikatech.voicesdktester.R;
import com.kikatech.voicesdktester.model.RetrofitManager;
import com.kikatech.voicesdktester.utils.google.cloud.speech.GoogleAesUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author SkeeterWang Created on 2018/6/28.
 */

public class VoiceConfig {
    private static final String TAG = "VoiceConfig";

    private static final boolean DEBUG_AUTH = false;

    private static String mGoogleAuthJson;

    public static void getVoiceConfig(Context ctx, VoiceConfiguration configuration, final IConfigListener listener) {
        if (TextUtils.isEmpty(mGoogleAuthJson)) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "google auth file json not found");
            }
            __getGoogleAuthFile(ctx, json -> {
                if (!TextUtils.isEmpty(json)) {
                    mGoogleAuthJson = json;
                    //configuration.setWebSocket(new GoogleApi(mGoogleAuthJson));
                    configuration.setWebSocket(new TencentApi(ctx));
                }
                dispatchConfiguration(listener, configuration);
            });
        } else {
            if (Logger.DEBUG) {
                Logger.v(TAG, "use cached google auth file json");
            }
            //configuration.setWebSocket(new GoogleApi(mGoogleAuthJson));
            configuration.setWebSocket(new TencentApi(ctx));
            dispatchConfiguration(listener, configuration);
        }
    }

    private static void __getGoogleAuthFile(final Context context, @NonNull final IGoogleAuthFileListener listener) {
        if (DEBUG_AUTH) {
            BackgroundThread.post(() -> listener.onLoaded(__getGoogleSpeechAuthJsonFromRaw(context)));
        } else {
            RetrofitManager.getGoogleAuthFileJson(data -> {
                String json = __getGoogleSpeechAuthJsonFromHttpResult(data);
                listener.onLoaded(json);
            });
        }
    }

    private static String __getGoogleSpeechAuthJsonFromRaw(Context context) {
        if (Logger.DEBUG) {
            Logger.i(TAG, "__getGoogleSpeechAuthJsonFromRaw");
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

    private static String __getGoogleSpeechAuthJsonFromHttpResult(String encodedString) {
        if (Logger.DEBUG) {
            Logger.i(TAG, "__getGoogleSpeechAuthJsonFromHttpResult");
        }
        if (TextUtils.isEmpty(encodedString)) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid encodedString");
            }
            return null;
        }
        if (Logger.DEBUG) {
            Logger.d(TAG, String.format("encodedString: %s", encodedString));
        }
        String decodedString = GoogleAesUtil.decrypt(encodedString);
        if (Logger.DEBUG) {
            Logger.d(TAG, String.format("decodedString: %s", decodedString));
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
