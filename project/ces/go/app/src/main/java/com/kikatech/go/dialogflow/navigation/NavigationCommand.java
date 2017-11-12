package com.kikatech.go.dialogflow.navigation;

/**
 * Created by bradchang on 2017/11/7.
 */

public class NavigationCommand {

    public final static byte NAVI_CMD_ERR               = 0x00;

    public final static byte NAVI_CMD_ASK_ADDRESS       = 0x10;
    public final static byte NAVI_CMD_ASK_ADDRESS_AGAIN = 0x11;
    public final static byte NAVI_CMD_CONFIRM_ADDRESS   = 0x12;
    public final static byte NAVI_CMD_DONT_UNDERSTAND   = 0x13;

    public final static byte NAVI_CMD_START_NAVI        = 0x20;
    public final static byte NAVI_CMD_STOP_NAVIGATION   = 0x21;

    public final static String NAVI_CMD_ADDRESS = "address";
}
