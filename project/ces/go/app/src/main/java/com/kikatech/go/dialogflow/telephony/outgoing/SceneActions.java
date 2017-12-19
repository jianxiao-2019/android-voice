package com.kikatech.go.dialogflow.telephony.outgoing;

/**
 * @author SkeeterWang Created on 2017/11/13.
 */
public class SceneActions {

    public static final String ACTION_OUTGOING_START = "telephony.outgoing.start";
    public static final String ACTION_OUTGOING_YES = "telephony.outgoing.yes";
    public static final String ACTION_OUTGOING_NO = "telephony.outgoing.no";
    //public static final String ACTION_OUTGOING_CHANGE = "telephony.outgoing.change";
    public static final String ACTION_OUTGOING_NUMBERS = "telephony.outgoing.numbers";
    public static final String ACTION_OUTGOING_CANCEL = "telephony.outgoing.cancel";

    public static final String PARAM_OUTGOING_NAME = "name";
    private static final String PARAM_OUTGOING_ORDINAL = "ordinal";
    private static final String PARAM_OUTGOING_NUMBER = "number";
    public static final String[] PARAM_OUTGOING_ORDINALS = new String[]{PARAM_OUTGOING_ORDINAL, PARAM_OUTGOING_NUMBER};
}
