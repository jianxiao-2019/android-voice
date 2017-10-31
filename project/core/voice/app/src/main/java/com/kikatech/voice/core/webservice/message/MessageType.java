package com.kikatech.voice.core.webservice.message;

/**
 * Created by tianli on 17-10-31.
 */

public enum MessageType {
    SEND("SEND"),
    PRESEND("PRESEND"),
    REPEAT("REPEAT"),
    CANCEL("CANCEL"),
    SPEECH("SPEECH"),
    ALTER("ALTER"),
    NOTALTERED("NOTALTERED"),
    TTSURL("TTS-URL"),
    TTSMARKS("TTS-MARKS");

    private String name;
    MessageType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static MessageType parseType(String type) {
        if (SEND.toString().equals(type)) {
            return SEND;
        } else if (PRESEND.toString().equals(type)) {
            return PRESEND;
        } else if (REPEAT.toString().equals(type)) {
            return REPEAT;
        } else if (CANCEL.toString().equals(type)) {
            return CANCEL;
        } else if (TTSURL.toString().equals(type)) {
            return TTSURL;
        } else if (TTSMARKS.toString().equals(type)) {
            return TTSMARKS;
        } else if (ALTER.toString().equals(type)) {
            return ALTER;
        } else if (NOTALTERED.toString().equals(type)) {
            return NOTALTERED;
        }
        return SPEECH;
    }
}
