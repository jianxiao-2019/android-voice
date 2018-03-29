package com.kikatech.voice.core.webservice.message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ryanlin on 29/03/2018.
 */

public class NBestMessage extends Message {

    public String[] text;   // n-best result

    @Override
    public void fromJson(JSONObject json) {
        String data = json.optString("data");
        try {
            parseTextResults(new JSONArray(data));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void parseData(JSONObject dataObj) {
    }

    private void parseTextResults(JSONArray array) throws JSONException {
        text = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            text[i] = array.getString(i);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " candidate count = " + text.length;
    }
}
