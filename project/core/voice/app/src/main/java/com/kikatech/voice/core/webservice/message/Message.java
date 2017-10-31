package com.kikatech.voice.core.webservice.message;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by tianli on 17-10-31.
 */

public abstract class Message {

    public static final int STATUS_ERROR = 0; // 0:服务器返回异常
    public static final int STATUS_SUCCESS = 1; // 1:服务器返回正常
    public static final int STATUS_CONN_ERROR = -1; // -1:连接错误（包括常见的联网超时错误）
    public static final int STATUS_EMPTY_DATA = -2; // -2:空包

    public static HashMap<String, Class<? extends Message>> TYPE_CLASS = new HashMap<>();

    public MessageType type;

    public int status; // 状态
    public String text; // server返回的识别结果
    public String engine; // server使用的引擎
    public String sessionId; // server分配的session id
    public int seqId; // server处理的顺序号，负数表示处理完成

    public Message() {
    }

    public void fromJson(JSONObject json) {
        status = json.optInt("state");
        text = json.optString("text");
        engine = json.optString("engine");
        sessionId = json.optString("cid");
        seqId = json.optInt("seq");
    }

    public static Message create(String type){
        Class<?> clazz = TYPE_CLASS.get(type);
        if(clazz != null){
            try {
                return (Message)clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void register(String type, Class<? extends Message> clazz){
        if(!TextUtils.isEmpty(type) && clazz != null){
            TYPE_CLASS.put(type, clazz);
        }
    }

}
