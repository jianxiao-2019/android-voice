package com.kikatech.voice.core.webservice.message;

import org.json.JSONObject;

/**
 * Created by ryanlin on 22/11/2017.
 */

public class EmojiRecommendMessage extends Message {

    public String emoji;
    public String descriptionText;
    public long cid;

    @Override
    protected void parseData(JSONObject dataObj) {
        emoji = dataObj.optString("key");
        descriptionText = dataObj.optString("value");
        cid = dataObj.optLong("cid");
    }

    @Override
    public String toString() {
        return super.toString() + " description = " + descriptionText;
    }
}
