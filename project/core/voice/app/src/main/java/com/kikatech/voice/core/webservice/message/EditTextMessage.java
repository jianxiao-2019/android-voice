package com.kikatech.voice.core.webservice.message;

import org.json.JSONObject;

/**
 * Created by tianli on 17-10-31.
 */

public class EditTextMessage extends Message {

    public int alterStart;
    public int alterEnd;

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        alterStart = json.optInt("alterStart", 0);
        alterEnd = json.optInt("alterEnd", 0);
    }
}
