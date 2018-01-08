package com.kikatech.go.eventbus;

/**
 * Events that send to {@link com.kikatech.go.services.DialogFlowForegroundService}
 *
 * @author SkeeterWang Created on 2017/12/1.
 */

public class ToDFServiceEvent extends BaseDFServiceEvent {

    public static final String ACTION_PING_SERVICE_STATUS = "action_ping_service_status";
    public static final String ACTION_ON_APP_FOREGROUND = "action_on_app_foreground";
    public static final String ACTION_ON_APP_BACKGROUND = "action_on_app_background";
    public static final String ACTION_ON_STATUS_CHANGED = "action_status_changed";
    public static final String ACTION_ON_MSG_CHANGED = "action_msg_changed";
    public static final String ACTION_ON_NAVIGATION_STARTED = "action_navigation_started";
    public static final String ACTION_ON_NAVIGATION_STOPPED = "action_navigation_stopped";
    public static final String ACTION_DIALOG_FLOW_TALK = "action_dialog_flow_talk";
    public static final String ACTION_DIALOG_FLOW_WAKE_UP = "action_dialog_flow_wake_up";
    public static final String ACTION_PING_VOICE_SOURCE = "action_ping_voice_source";
    public static final String ACTION_INVERT_WAKE_UP_DETECTOR_ABILITY = "action_invert_wake_up_detector_ability";
    public static final String ACTION_SWITCH_WAKE_UP_SCENE = "action_switch_wake_up_scene";
    public static final String ACTION_BLUETOOTH_EVENT = "action_bluetooth_event";
    public static final String ACTION_ACCESSIBILITY_STARTED = "action_accessibility_started";
    public static final String ACTION_ACCESSIBILITY_STOPPED = "action_accessibility_stopped";

    public static final String PARAM_STATUS = "param_status";
    public static final String PARAM_TEXT = "param_text";

    public ToDFServiceEvent(String action) {
        super(action);
    }
}
