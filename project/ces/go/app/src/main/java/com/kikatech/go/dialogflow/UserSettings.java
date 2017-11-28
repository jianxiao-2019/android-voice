package com.kikatech.go.dialogflow;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class UserSettings {

    //TODO Define following variables in Settings
    public final static byte SETTING_REPLY_SMS_IGNORE = 0;
    public final static byte SETTING_REPLY_SMS_READ = 1;
    public final static byte SETTING_REPLY_SMS_ASK_USER = 2;

    public static byte getReplyMessageSetting() {
        return SETTING_REPLY_SMS_ASK_USER;
    }
}
