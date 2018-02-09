package com.kikatech.go.eventbus;

/**
 * @author SkeeterWang Created on 2018/2/9.
 */

public class ToOobeServiceEvent extends BaseDFServiceEvent {

    public static final String ACTION_SHOW_OOBE_UI = "action_show_oobe_ui";
    public static final String ACTION_HIDE_OOBE_UI = "action_hide_oobe_ui";

    public ToOobeServiceEvent(String action) {
        super(action);
    }
}
