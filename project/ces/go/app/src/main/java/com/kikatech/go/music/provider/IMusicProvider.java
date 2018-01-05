package com.kikatech.go.music.provider;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public interface IMusicProvider {
    void play();

    void pause();

    void resume();

    void mute();

    void unmute();

    void stop();
}
