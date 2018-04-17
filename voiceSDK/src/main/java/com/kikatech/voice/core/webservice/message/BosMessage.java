package com.kikatech.voice.core.webservice.message;

import org.json.JSONObject;

/**
 * Created by ryanlin on 16/04/2018.
 */

public class BosMessage extends Message {

    public long cid = -1;          // conversation id

    @Override
    public void fromJson(JSONObject json) {
        String data = json.optString("data");
        try {
            cid = Long.valueOf(data);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void parseData(JSONObject dataObj) {
    }

    @Override
    public String toString() {
        return super.toString() + "(" + cid + ")";
    }
}
