package com.kikatech.go.dialogflow.sms.send;

import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SceneActions {

    public static final String ACTION_SEND_SMS = "send.sms";
    public static final String ACTION_SEND_SMS_NAME = "SendSMS.SendSMS-name";
    public static final String ACTION_SEND_SMS_MSGBODY = Intent.ACTION_ANY_WORDS;
    public static final String ACTION_SEND_SMS_NO = "SendSMS.SendSMS-no";
    public static final String ACTION_SEND_SMS_YES = "SendSMS.SendSMS-yes";
    public static final String ACTION_SEND_SMS_CANCEL = "SendSMS.SendSMS-cancel";
    public static final String ACTION_SEND_SMS_SELECT_NUM = "SendSMS.SendSMS-selectnumber";
    public static final String ACTION_SEND_SMS_CHANGE_SMS_BODY = "SendSMS.SendSMS-chnage.sms";
    public static final String ACTION_SEND_SMS_AGAIN = "SendSMS.SendSMS-repeat";
}
