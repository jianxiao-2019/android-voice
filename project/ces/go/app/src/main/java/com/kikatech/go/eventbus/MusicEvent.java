package com.kikatech.go.eventbus;

/**
 * @author SkeeterWang Created on 2018/4/11.
 */

public class MusicEvent extends BaseEvent {

    public static final String ACTION_ON_START = "action_on_music_start";
    public static final String ACTION_ON_RESUME = "action_on_music_resume";
    public static final String ACTION_ON_PAUSE = "action_on_music_pause";
    public static final String ACTION_ON_STOP = "action_on_music_stop";

    public MusicEvent(String action) {
        super(action);
    }
}
