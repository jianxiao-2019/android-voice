package com.kikatech.voice.core.webservice.command;

/**
 * @author SkeeterWang Created on 2018/6/13.
 */

public class SocketCommand {
    public static final String NBEST = "NBEST";
    public static final String ALTERING = "ALTERING";
    public static final String SETTINGS = "SETTINGS";
    public static final String TOKEN = "TOKEN";
    public static final String STOP = "STOP";            // stop and drop current results
    public static final String RESET = "RESET";          // stop, drop current results and start new conversation
    public static final String COMPLETE = "COMPLETE";    // stop and wait final results
    public static final String ALIGNMENT = "ALIGNMENT";
}
