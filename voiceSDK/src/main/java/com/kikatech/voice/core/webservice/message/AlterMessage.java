package com.kikatech.voice.core.webservice.message;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by ryanlin on 2018/5/22.
 */

public class AlterMessage extends Message {

    public static final int TYPE_PRONOUNCE = 1;
    public static final int TYPE_DUPLICATED_HOOKS = 2;

    public String[] text;   // "text, or other candidates"
    public String context;  // "Send me a taxi in 5 minutes"
    public String altered;  // "text"
    public long cid;
    public long endCid;
    public int alterStart;  // 10
    public int alterEnd;    // 14
    public int type;

    @Override
    protected void parseData(JSONObject dataObj) {

        context = dataObj.optString("context");
        altered = dataObj.optString("altered");

        alterStart = dataObj.optInt("alterStart");
        alterEnd = dataObj.optInt("alterEnd");

        type = dataObj.optInt("type");

        cid = dataObj.optLong("cid");
        endCid = dataObj.optLong("endCid");
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
                    + "\n(" + cid + ")(" + type + ")";
        } else {
            return super.toString();
        }
    }
}
