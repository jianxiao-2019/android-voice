package com.kikatech.voice.core.dialogflow.constant;

/**
 * @author SkeeterWang Created on 2017/11/8.
 */
public class TelephonyOutgoingCommand {

    public static final byte TELEPHONY_OUTGOING_CMD_ERR = 0x00;

    /**
     * ex: who do you want to call?
     */
    public static final byte TELEPHONY_OUTGOING_CMD_ASK_NAME = 0x01;
    /**
     * ex: Could not find in contacts. Please say it again.
     */
    public static final byte TELEPHONY_OUTGOING_CMD_CONTACT_NOT_FOUND = 0x02;
    /**
     * ex: do you mean Skeeter?
     */
    public static final byte TELEPHONY_OUTGOING_CMD_CONFIRM_NAME = 0x03;
    public final static byte TELEPHONY_OUTGOING_CMD_DONT_UNDERSTAND = 0x04;

    public static final byte TELEPHONY_OUTGOING_CMD_START_CALL = 0x11;
    public final static byte TELEPHONY_OUTGOING_CMD_CANCELED = 0x12;

    public static final String TELEPHONY_OUTGOING_CMD_NAME = "name";
    public static final String TELEPHONY_OUTGOING_CMD_NUMBER = "number";
}
