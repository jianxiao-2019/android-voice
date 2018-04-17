package com.kikatech.voice.core.webservice.message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tianli on 17-10-31.
 */

public class TextMessage extends Message {

    public int state;           // status
    public String[] text;       // asr result
    public String engine;       // asr engine
    public long cid;          // conversation id

    public TextMessage() {
    }

    public TextMessage(IntermediateMessage intermediateMessage) {
        if (intermediateMessage != null) {
            this.state = intermediateMessage.state;
            this.text = new String[]{intermediateMessage.text};
            this.engine = intermediateMessage.engine;
            this.cid = intermediateMessage.cid;
        }
    }

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
        text = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            text[i] = array.getString(i);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " text = " + text[0] + "(" + cid + ")";
    }
}
