package com.kikatech.voice.core.webservice.message;

import org.json.JSONObject;

/**
 * Created by tianli on 17-10-31.
 */

public class TextMessage extends Message {

    public int state;           // status
    public String text;   // asr result
    public String engine;       // asr engine
    public String cid;          // conversation id

    @Override
    protected void parseData(JSONObject dataObj) {
        state = dataObj.optInt("state");
        text = dataObj.optString("transcript");
        engine = dataObj.optString("engine");
        cid = dataObj.optString("cid");
    }

    @Override
    public String toString() {
        return super.toString() + " text = " + text;
    }
}
