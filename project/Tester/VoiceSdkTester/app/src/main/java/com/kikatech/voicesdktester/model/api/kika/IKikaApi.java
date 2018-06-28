package com.kikatech.voicesdktester.model.api.kika;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author SkeeterWang Created on 2018/6/28.
 */
public interface IKikaApi {
    String DOMAIN = "http://api-dev.kika.ai/v3/";

    @GET("auth/getGoogleKey")
    Call<ResponseBody> getGoogleAuthFileJson();
}
