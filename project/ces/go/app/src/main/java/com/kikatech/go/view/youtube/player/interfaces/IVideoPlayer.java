package com.kikatech.go.view.youtube.player.interfaces;

/**
 * @author SkeeterWang Created on 2018/1/18.
 */

public interface IVideoPlayer {

    void start();

    void pause();

    void resume();

    void stop();

    void release(boolean clearTargetState);

    void seekTo(int position);

    int getDuration();

    int getCurrentPosition();

    int getBufferPercentage();

    boolean isPlaying();

    boolean isPrepared();
}
