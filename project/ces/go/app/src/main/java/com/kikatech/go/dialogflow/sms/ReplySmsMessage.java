package com.kikatech.go.dialogflow.sms;

import com.kikatech.go.dialogflow.EmojiMessage;
import com.kikatech.go.message.sms.SmsObject;

/**
 * Created by brad_chang on 2017/12/7.
 */

public class ReplySmsMessage extends EmojiMessage {

    private SmsObject mSmsObject;

    public void updateSmsObject(SmsObject smsObject) {
        mSmsObject = smsObject;
    }

    public SmsObject getSmsObject() {
        return mSmsObject;
    }

    public String getPhoneNumber() {
        return mSmsObject.getId();
    }
}
