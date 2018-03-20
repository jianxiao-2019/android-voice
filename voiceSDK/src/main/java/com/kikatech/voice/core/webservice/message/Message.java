package com.kikatech.voice.core.webservice.message;

import android.text.TextUtils;

import com.kikatech.voice.util.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by tianli on 17-10-31.
 */

public abstract class Message {

    public static final HashMap<String, Class<? extends Message>> TYPE_CLASS = new HashMap<>();

    public static final String MSG_TYPE_INTERMEDIATE = "INTERMEDIATE";
    public static final String MSG_TYPE_ALTER = "ALTER";
    public static final String MSG_TYPE_ASR = "ASR";
    public static final String MSG_TYPE_EMOJI = "EMOJI";

    public Message() {
    }

    public void fromJson(JSONObject json) {
        String data = json.optString("data");
        Logger.d("123 Message fromJson data = " + data);
        if (!TextUtils.isEmpty(data) && !"null".equals(data)) {
            try {
                parseData(new JSONObject(data));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void parseData(JSONObject dataObj);

    public static Message create(String type) {
        Class<?> clazz = TYPE_CLASS.get(type);
        if (clazz != null) {
            try {
                return (Message) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void register(String type, Class<? extends Message> clazz) {
        if (!TextUtils.isEmpty(type) && clazz != null) {
            TYPE_CLASS.put(type, clazz);
        }
    }

    public static void unregisterAll() {
        TYPE_CLASS.clear();
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + "]";
    }
}
