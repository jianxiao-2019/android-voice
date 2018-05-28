package com.kikatech.voice.core.webservice.message;

import org.json.JSONArray;
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

    public TextMessage(int state, String[] text, String engine, long cid) {
        this.state = state;
        this.text = text;
        this.engine = engine;
        this.cid = cid;
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
        parseTextResults(dataObj.optJSONArray("transcripts"));
    }

    private void parseTextResults(JSONArray array) {
        if (array != null && array.length() > 0) {
            text = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                text[i] = array.optString(i);
            }
        } else {
            text = new String[]{""};
        }
    }

    @Override
    public String toString() {
        if (text != null && text[0] != null) {
            return super.toString() + " text = " + text[0] + "(" + cid + ")";
        } else {
            return super.toString();
        }
    }
}
