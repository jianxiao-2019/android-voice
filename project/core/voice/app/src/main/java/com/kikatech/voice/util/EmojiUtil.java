package com.kikatech.voice.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brad_chang on 2017/12/5.
 */

public class EmojiUtil {

    public final static String KEY_EMOJI_CODE = "emoji_code";
    public final static String KEY_EMOJI_DESC = "emoji_desc";

    public static String composeJsonString(String emoji, String descriptionText) {
        JSONObject emjs = new JSONObject();
        try {
            emjs.put(KEY_EMOJI_CODE, emoji);
            emjs.put(KEY_EMOJI_DESC, descriptionText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return emjs.toString();
    }
}
