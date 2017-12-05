package com.kikatech.voice.core.webservice.message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tianli on 17-10-31.
 */

public class EditTextMessage extends Message {

    public int alterStart;
    public int alterEnd;
    public String context;
    public String altered;
    public String[] text;
    public long cid;

    @Override
    protected void parseData(JSONObject dataObj) {
        alterStart = dataObj.optInt("alterStart", 0);
        alterEnd = dataObj.optInt("alterEnd", 0);
        context = dataObj.optString("context");
        altered = dataObj.optString("altered");
        cid = dataObj.optLong("cid");
        try {
            parseTextResults(dataObj.getJSONArray("transcripts"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseTextResults(JSONArray array) throws JSONException {
        text = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            text[i] = array.getString(i);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " text = " + text;
    }
}
