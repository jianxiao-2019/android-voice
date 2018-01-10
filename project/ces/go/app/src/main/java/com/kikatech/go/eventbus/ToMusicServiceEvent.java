package com.kikatech.go.eventbus;

/**
 * Events that send to {@link com.kikatech.go.services.MusicForegroundService}
 *
 * @author jason Created on 2018/1/10.
 */

public class ToMusicServiceEvent {

    public static final String ACTION_MUSIC_CHANGE = "action_music_change";

    private String action;

    public ToMusicServiceEvent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
