package com.kikatech.go.dialogflow.sms.reply;

import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class SceneActions {

    public static final String ACTION_REPLY_SMS = "reply.sms";
    public static final String ACTION_REPLY_SMS_YES = "ReplySMS.ReplySMS-yes";
    public static final String ACTION_REPLY_SMS_NO = "ReplySMS.ReplySMS-no";
    public static final String ACTION_REPLY_SMS_CANCEL = "ReplySMS.ReplySMS-cancel";
    public static final String ACTION_REPLY_SMS_MSG_BODY = Intent.ACTION_USER_INPUT;
    public static final String ACTION_REPLY_SMS_CHANGE = "ReplySMS.ReplySMS-change";
}