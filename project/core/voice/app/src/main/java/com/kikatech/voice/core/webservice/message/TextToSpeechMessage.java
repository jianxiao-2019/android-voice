package com.kikatech.voice.core.webservice.message;

import org.json.JSONObject;

/**
 * Created by tianli on 17-10-31.
 */

public class TextToSpeechMessage extends Message {

    public String url;

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        url = json.optString("url");
    }
}
