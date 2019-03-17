package com.kikatech.voicesdktester.model.api.kika;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author SkeeterWang Created on 2018/6/28.
 */
public interface IKikaApi {
    String DOMAIN = "http://dev.kikago.ai/v1/";

    @GET("auth/GoogleKey")
    Call<ResponseBody> getGoogleAuthFileJson();
}
