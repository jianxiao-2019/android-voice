package com.kikatech.go.dialogflow.im.send;

import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class SceneActions {
    public static final String ACTION_SEND_IM = "send.message";
    public static final String ACTION_SEND_IM_APP = "SendIM.SendIM-im.app";
    public static final String ACTION_SEND_IM_NAME = "SendIM.SendIM-name";
    public static final String ACTION_SEND_IM_MSGBODY = Intent.ACTION_USER_INPUT;
    public static final String ACTION_SEND_IM_NO = "SendIM.SendIM-no";
    public static final String ACTION_SEND_IM_YES = "SendIM.SendIM-yes";
    public static final String ACTION_SEND_IM_CANCEL = "SendIM.SendIM-cancel";
    public static final String ACTION_SEND_IM_SELECT_NUM = "SendIM.SendIM-selectnumber";
    public static final String ACTION_SEND_IM_CHANGE_IM_BODY = "SendIM.SendIM-change.im";
    public static final String ACTION_SEND_IM_FALLBACK = "SendIM.SendIM-fallback";
}
