package com.kikatech.voicesdktester.model;

import android.support.annotation.NonNull;

import com.kikatech.voice.util.log.Logger;
import com.kikatech.voicesdktester.model.api.kika.IKikaApi;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author SkeeterWang Created on 2018/6/28.
 */

public class RetrofitManager {
    private static final String TAG = "RetrofitManager";

    public static void getGoogleAuthFileJson(IRetrofitListener<String> listener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IKikaApi.DOMAIN)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IKikaApi api = retrofit.create(IKikaApi.class);
        api.getGoogleAuthFileJson().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                ResponseBody body = response.body();
                String encodedResult = null;
                try {
                    encodedResult = body != null ? body.string() : null;
                } catch (Exception e) {
                    if (Logger.DEBUG) {
                        Logger.printStackTrace(TAG, e.getMessage(), e);
                    }
                }
                if (Logger.DEBUG) {
                    Logger.v(TAG, String.format("encodedResult: %s", encodedResult));
                }
                if (listener != null) {
                    listener.onResponse(encodedResult);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                if (listener != null) {
                    listener.onResponse(null);
                }
            }
        });
    }

    public interface IRetrofitListener<T> {
        void onResponse(T data);
    }
}
