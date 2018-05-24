package com.kikatech.voice.core.webservice.message;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tianli on 17-10-31.
 */

public abstract class Message {

    public static final String MSG_TYPE_INTERMEDIATE = "INTERMEDIATE";
    public static final String MSG_TYPE_ALTER = "ALTER";
    public static final String MSG_TYPE_ASR = "ASR";
    public static final String MSG_TYPE_EMOJI = "EMOJI";
    public static final String MSG_TYPE_NBEST = "NBEST";

    public static final String MSG_TYPE_BOS = "BOS";
    public static final String MSG_TYPE_EOS = "EOS";

    public Message() {
    }

    public void fromJson(JSONObject json) {
        String data = json.optString("data");
        if (!TextUtils.isEmpty(data) && !"null".equals(data)) {
            try {
                parseData(new JSONObject(data));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void parseData(JSONObject dataObj);

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + "]";
    }
}
