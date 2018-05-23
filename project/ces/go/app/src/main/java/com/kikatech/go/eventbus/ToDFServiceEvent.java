package com.kikatech.go.eventbus;

/**
 * Events that send to {@link com.kikatech.go.services.DialogFlowForegroundService}
 *
 * @author SkeeterWang Created on 2017/12/1.
 */

public class ToDFServiceEvent extends BaseEvent {

    public static final String ACTION_CHANGE_SERVER = "action_change_server";
    public static final String ACTION_PING_SERVICE_STATUS = "action_ping_service_status";
    public static final String ACTION_SCAN_USB_DEVICES = "action_scan_usb_devices";
    public static final String ACTION_ON_APP_FOREGROUND = "action_on_app_foreground";
    public static final String ACTION_ON_APP_BACKGROUND = "action_on_app_background";
    public static final String ACTION_ON_STATUS_CHANGED = "action_status_changed";
    public static final String ACTION_ON_MSG_CHANGED = "action_msg_changed";
    public static final String ACTION_ON_NAVIGATION_STARTED = "action_navigation_started";
    public static final String ACTION_ON_NAVIGATION_STOPPED = "action_navigation_stopped";
    public static final String ACTION_DIALOG_FLOW_TALK = "action_dialog_flow_talk";
    public static final String ACTION_DIALOG_FLOW_WAKE_UP = "action_dialog_flow_wake_up";
    public static final String ACTION_PING_VOICE_SOURCE = "action_ping_voice_source";
    public static final String ACTION_ACCESSIBILITY_STARTED = "action_accessibility_started";
    public static final String ACTION_ACCESSIBILITY_STOPPED = "action_accessibility_stopped";
    public static final String ACTION_DISABLE_WAKE_UP_DETECTOR = "action_disable_wake_up_detector";
    public static final String ACTION_ENABLE_WAKE_UP_DETECTOR = "action_enable_wake_up_detector";
    public static final String ACTION_ON_NEW_MSG = "action_on_new_msg";
    public static final String ACTION_DO_TUTORIAL = "action_do_tutorial";
    public static final String ACTION_STOP_TUTORIAL = "action_stop_tutorial";
    public static final String ACTION_SET_VOLUME = "action_set_volume";

    public static final String PARAM_STATUS = "param_status";
    public static final String PARAM_TEXT = "param_text";
    public static final String PARAM_MSG_COMMEND = "param_msg_commend";
    public static final String PARAM_TIMESTAMP = "param_timestamp";
    public static final String PARAM_VOLUME = "param_volume";

    public ToDFServiceEvent(String action) {
        super(action);
    }
}
