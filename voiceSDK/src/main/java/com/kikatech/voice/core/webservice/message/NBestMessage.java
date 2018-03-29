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
    protected void parseData(JSONObject dataObj) {
        try {
            parseTextResults(dataObj.getJSONArray("data"));
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
}
