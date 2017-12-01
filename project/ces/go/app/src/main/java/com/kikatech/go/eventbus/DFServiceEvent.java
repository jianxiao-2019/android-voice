package com.kikatech.go.eventbus;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Events that send by {@link com.kikatech.go.services.DialogFlowForegroundService}
 *
 * @author SkeeterWang Created on 2017/12/1.
 */

public class DFServiceEvent {
    public static final String ACTION_ON_DIALOG_FLOW_INIT = "action_on_dialog_flow_init";
    public static final String ACTION_ON_ASR_RESULT = "action_on_asr_result";
    public static final String ACTION_ON_TEXT = "action_on_text";
    public static final String ACTION_ON_TEXT_PAIRS = "action_on_text_pairs";
    public static final String ACTION_ON_STAGE_PREPARED = "action_on_stage_prepared";
    public static final String ACTION_ON_STAGE_ACTION_DONE = "action_on_stage_action_done";
    public static final String ACTION_ON_STAGE_EVENT = "action_on_stage_event";
    public static final String ACTION_ON_SCENE_EXIT = "action_on_scene_exit";
    public static final String ACTION_ON_AGENT_QUERY_START = "action_on_agent_query_start";
    public static final String ACTION_ON_AGENT_QUERY_STOP = "action_on_agent_query_stop";
    public static final String ACTION_ON_AGENT_QUERY_ERROR = "action_on_agent_query_error";

    public static final String PARAM_EXTRAS = "param_extras";
    public static final String PARAM_TEXT = "param_text";
    public static final String PARAM_IS_FINISHED = "param_is_finished";
    public static final String PARAM_SCENE = "param_scene";
    public static final String PARAM_SCENE_ACTION = "param_scene_action";
    public static final String PARAM_SCENE_STAGE = "param_scene_stage";
    public static final String PARAM_IS_INTERRUPTED = "param_is_interrupted";

    private String action;
    private Bundle extras = new Bundle();

    public DFServiceEvent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public Bundle getExtras() {
        return extras;
    }

    public void putExtra(String key, String value) {
        extras.putString(key, value);
    }

    public void putExtra(String key, boolean value) {
        extras.putBoolean(key, value);
    }

    public void putExtra(String key, Bundle value) {
        extras.putBundle(key, value);
    }

    public void putExtra(String key, Serializable value) {
        extras.putSerializable(key, value);
    }
}
