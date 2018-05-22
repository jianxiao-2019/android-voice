package com.kikatech.voice.core.webservice.message;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by ryanlin on 2018/5/22.
 */

public class AlterMessage extends Message {

    public String[] text;   // "text, or other candidates"
    public String context;  // "Send me a taxi in 5 minutes"
    public String altered;  // "text"
    public long cid;
    public int alterStart;  // 10
    public int alterEnd;    // 14

    @Override
    protected void parseData(JSONObject dataObj) {

        context = dataObj.optString("context");
        altered = dataObj.optString("altered");

        alterStart = dataObj.optInt("alterStart");
        alterEnd = dataObj.optInt("alterEnd");

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
            return super.toString() + " text = " + text[0]
                    + "\ncontext <" + context + ">"
                    + "\naltered <" + altered + ">"
                    + "\nAt[" + alterStart + "," + alterEnd + "]"
                    + "\n(" + cid + ")";
        } else {
            return super.toString();
        }
    }
}
