package com.kikatech.voice.core.webservice.message;

import org.json.JSONObject;

/**
 * Created by tianli on 17-10-31.
 */

public class EditTextMessage extends Message {

    public int alterStart;
    public int alterEnd;
    public String context;
    public String altered;
    public String text;

    @Override
    protected void parseData(JSONObject dataObj) {
        text = dataObj.optString("transcript");
        alterStart = dataObj.optInt("alterStart", 0);
        alterEnd = dataObj.optInt("alterEnd", 0);
        context = dataObj.optString("context");
        altered = dataObj.optString("altered");
    }

    @Override
    public String toString() {
        return super.toString() + " text = " + text;
    }
}
