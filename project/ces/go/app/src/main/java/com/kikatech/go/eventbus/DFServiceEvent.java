package com.kikatech.go.eventbus;

/**
 * Events that send by {@link com.kikatech.go.services.DialogFlowForegroundService}
 *
 * @author SkeeterWang Created on 2017/12/1.
 */

public class DFServiceEvent extends BaseEvent {
    public static final String ACTION_ON_CONNECTIVITY_CHANGED = "action_on_connectivity_changed";
    public static final String ACTION_ON_PING_SERVICE_STATUS = "action_on_ping_service_status";
    public static final String ACTION_ON_USB_NO_DEVICES = "action_on_usb_no_device";
    public static final String ACTION_EXIT_APP = "action_exit_app";
    public static final String ACTION_ON_WAKE_UP = "action_on_wake_up";
    public static final String ACTION_ON_SLEEP = "action_on_sleep";
    public static final String ACTION_ON_DIALOG_FLOW_INIT = "action_on_dialog_flow_init";
    public static final String ACTION_ON_ASR_PAUSE = "action_on_asr_pause";
    public static final String ACTION_ON_ASR_RESUME = "action_on_asr_resume";
    public static final String ACTION_ON_ASR_RESULT = "action_on_asr_result";
    public static final String ACTION_ON_TEXT = "action_on_text";
    public static final String ACTION_ON_TEXT_PAIRS = "action_on_text_pairs";
    public static final String ACTION_ON_STAGE_PREPARED = "action_on_stage_prepared";
    public static final String ACTION_ON_STAGE_ACTION_DONE = "action_on_stage_action_done";
    public static final String ACTION_ON_STAGE_EVENT = "action_on_stage_event";
    public static final String ACTION_ON_SCENE_EXIT = "action_on_scene_exit";
    public static final String ACTION_ON_AGENT_QUERY_START = "action_on_agent_query_start";
    public static final String ACTION_ON_AGENT_QUERY_COMPLETE = "action_on_agent_query_done";
    public static final String ACTION_ON_AGENT_QUERY_ERROR = "action_on_agent_query_error";
    public static final String ACTION_ON_ASR_CONFIG = "action_on_asr_config";
    public static final String ACTION_ON_VOICE_SRC_CHANGE = "action_on_voice_source_change";
    public static final String ACTION_ON_USB_DEVICE_DATA_STATUS_CHANGED = "action_on_usb_device_data_status_changed";

    public static final String PARAM_WAKE_UP_FROM = "param_wake_up_from";
    public static final String PARAM_EXTRAS = "param_extras";
    public static final String PARAM_TEXT = "param_text";
    public static final String PARAM_IS_FINISHED = "param_is_finished";
    public static final String PARAM_SCENE = "param_scene";
    public static final String PARAM_SCENE_ACTION = "param_scene_action";
    public static final String PARAM_SCENE_STAGE = "param_scene_stage";
    public static final String PARAM_BOS_DURATION = "param_bos_duration";
    public static final String PARAM_IS_PROACTIVE = "param_is_proactive";
    public static final String PARAM_SERVICE_STATUS = "param_service_status";
    public static final String PARAM_IS_USB_DEVICE_DATA_CORRECT = "param_is_usb_device_data_correct";

    public static final String PARAM_DBG_INTENT_ACTION = "param_dbg_intent_action";
    public static final String PARAM_DBG_INTENT_PARMS = "param_dbg_intent_parms";

    public DFServiceEvent(String action) {
        super(action);
    }
}
