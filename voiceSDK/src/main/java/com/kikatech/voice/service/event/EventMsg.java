package com.kikatech.voice.service.event;

/**
 * Created by ryanlin on 11/01/2018.
 */

public class EventMsg {
    public Type type;
    public Object obj;

    public EventMsg(Type type) {
        this(type, null);
    }

    public EventMsg(Type type, Object obj) {
        this.type = type;
        this.obj = obj;
    }

    public enum Type {
        VD_VAD_CHANGED,
    }
}
