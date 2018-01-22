package com.kikatech.go.music.provider;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public interface IMusicProvider<T extends Object> {
    void play(T musicModel);

    void pause();

    void resume();

    void volumeUp();

    void volumeDown();

    void mute();

    void unmute();

    void stop();

    boolean isPlaying();

    boolean isPrepared();
}
