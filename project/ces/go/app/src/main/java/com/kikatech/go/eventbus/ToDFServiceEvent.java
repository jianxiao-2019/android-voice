package com.kikatech.go.eventbus;

/**
 * Events that send to {@link com.kikatech.go.services.DialogFlowForegroundService}
 *
 * @author SkeeterWang Created on 2017/12/1.
 */

public class ToDFServiceEvent extends BaseDFServiceEvent {

    public static final String ACTION_ON_STATUS_CHANGED = "action_status_changed";
    public static final String ACTION_ON_NAVIGATION_STARTED = "action_navigation_started";
    public static final String ACTION_ON_NAVIGATION_STOPPED = "action_navigation_stopped";
    public static final String ACTION_DIALOG_FLOW_TALK = "action_dialog_flow_talk";

    public static final String PARAM_STATUS = "param_status";
    public static final String PARAM_TEXT = "param_text";

    public ToDFServiceEvent(String action) {
        super(action);
    }
}
