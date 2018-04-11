package com.kikatech.go.eventbus;

/**
 * Events that send to {@link com.kikatech.go.services.MusicForegroundService}
 *
 * @author jason Created on 2018/1/10.
 */

public class ToMusicServiceEvent extends BaseEvent {

    public static final String ACTION_MUSIC_CHANGE = "action_music_change";
    public static final String ACTION_VOLUME_CONTROL = "action_volume_control";
    public static final String ACTION_PAUSE_MUSIC = "action_pause_music";
    public static final String ACTION_RESUME_MUSIC = "action_resume_music";

    public static final String PARAM_VOLUME_CONTROL_TYPE = "param_volume_control_type";

    public ToMusicServiceEvent(String action) {
        super(action);
    }
}
