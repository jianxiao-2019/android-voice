package com.kikatech.voice.core.dialogflow.intent;

/**
 * Created by tianli on 17-10-27.
 */

public enum Scene {

    Telephony("telephony"),
    IM("im"),
    SMS("sms"),
    Navigation("navigation");

    private String mType;

    Scene(String type) {
        mType = type;
    }

    @Override
    public String toString() {
        return mType;
    }

}
