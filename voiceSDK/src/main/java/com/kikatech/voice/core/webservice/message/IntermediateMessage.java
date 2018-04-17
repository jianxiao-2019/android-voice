package com.kikatech.voice.core.webservice.message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ryanlin on 10/11/2017.
 */

public class IntermediateMessage extends Message {

    public int state;           // status
    public String text;   // asr result
    public String engine;       // asr engine
    public long cid;          // conversation id

    @Override
    protected void parseData(JSONObject dataObj) {
        state = dataObj.optInt("state");
        engine = dataObj.optString("engine");
        cid = dataObj.optLong("cid");
        try {
            parseTextResults(dataObj.getJSONArray("transcripts"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseTextResults(JSONArray array) throws JSONException {
        text = array.getString(0);
    }

    @Override
    public String toString() {
        return super.toString() + " text = " + text + "(" + cid + ")";
    }
}
