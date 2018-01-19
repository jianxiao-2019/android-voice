package com.kikatech.go.view.youtube.interfaces;

/**
 * @author SkeeterWang Created on 2017/6/20.
 */

public interface VideoPlayer {
    void start();

    void pause();

    int getDuration();

    /**
     * @return current playback position in milliseconds.
     */
    int getCurrentPosition();

    void seekTo(int position);

    boolean isPlaying();

    int getBufferPercentage();

    boolean canPause();

    boolean canSeekBackward();

    boolean canSeekForward();

    /**
     * Get the audio session id for the player used by this VideoView. This can be used to
     * apply audio effects to the audio track of a video.
     *
     * @return The audio session, or 0 if there was an error.
     */
    int getAudioSessionId();
}
