package com.kikatech.voice.dialogflow.telephony;

/**
 * @author SkeeterWang Created on 2017/11/7.
 */
public class TelephonyIncomingCommand {

    public static final byte TELEPHONY_INCOMING_CMD_ERR = 0x00;

    public static final byte TELEPHONY_INCOMING_CMD_PRE_START = 0x01;

    public static final byte TELEPHONY_INCOMING_CMD_START = 0x10;
    public static final byte TELEPHONY_INCOMING_CMD_ANSWER = 0x11;
    public static final byte TELEPHONY_INCOMING_CMD_REJECT = 0x12;
    public static final byte TELEPHONY_INCOMING_CMD_IGNORE = 0x13;

    public static final String TELEPHONY_INCOMING_CMD_NAME = "name";
}
