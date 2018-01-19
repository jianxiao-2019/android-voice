package com.kikatech.go.eventbus;

/**
 * Events that send to {@link com.kikatech.go.services.MusicForegroundService}
 *
 * @author jason Created on 2018/1/10.
 */

public class ToMusicServiceEvent extends BaseDFServiceEvent {

    public static final String ACTION_PLAY_SONG = "action_play_song";
    public static final String ACTION_MUSIC_CHANGE = "action_music_change";

    public static final String PARAM_PLAY_LIST = "param_play_list";

    public ToMusicServiceEvent(String action) {
        super(action);
    }
}
