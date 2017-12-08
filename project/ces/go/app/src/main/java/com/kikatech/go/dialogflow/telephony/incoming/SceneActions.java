package com.kikatech.go.dialogflow.telephony.incoming;

/**
 * Created by tianli on 17-11-11.
 */

public class SceneActions {

    public static final String ACTION_INCOMING_START = "telephony.incoming.start";
    public static final String ACTION_INCOMING_ANSWER = "telephony.incoming.answer";
    public static final String ACTION_INCOMING_REJECT = "telephony.incoming.reject";
    public static final String ACTION_INCOMING_IGNORE = "telephony.incoming.ignore";

    public final static String PARAM_INCOMING_NAME = "name";

    public static final String KIKA_PROCESS_INCOMING_CALL = "kika_process_incoming_call %s";

}