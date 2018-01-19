package com.kikatech.go.view.youtube.interfaces;

/**
 * @author SkeeterWang Created on 2017/6/20.
 */

public interface VideoPlayerController {
    void setMediaPlayer(VideoPlayer fensterPlayer);

    void setEnabled(boolean value);

    void show(int timeInMilliSeconds);

    void show();

    void hide();

    void setVisibilityListener(VideoPlayerControllerVisibilityListener visibilityListener);
}
