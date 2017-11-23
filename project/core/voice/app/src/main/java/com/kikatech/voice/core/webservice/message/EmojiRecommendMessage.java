package com.kikatech.voice.core.webservice.message;

import org.json.JSONObject;

/**
 * Created by ryanlin on 22/11/2017.
 */

public class EmojiRecommendMessage extends Message {

    public String emoji;
    public String descriptionText;

    @Override
    protected void parseData(JSONObject dataObj) {
        emoji = dataObj.optString("type");
        descriptionText = dataObj.optString("value");
    }

    @Override
    public String toString() {
        return super.toString() + " description = " + descriptionText;
    }
}
