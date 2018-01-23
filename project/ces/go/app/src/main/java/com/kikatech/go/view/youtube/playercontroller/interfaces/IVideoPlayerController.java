package com.kikatech.go.view.youtube.playercontroller.interfaces;

import com.kikatech.go.view.youtube.player.interfaces.IVideoPlayer;

/**
 * @author SkeeterWang Created on 2018/1/19.
 */

public interface IVideoPlayerController {

    void setMediaPlayer(IVideoPlayer videoPlayer);

    void setEnabled(boolean value);

    void show();

    void show(int timeInMilliSeconds);

    void hide();

    void showLoadingView();

    void hideLoadingView();

    void setVisibilityListener(IVisibilityChangedListener visibilityListener);

    interface IVisibilityChangedListener {
        void onControlsVisibilityChange(boolean isShowing);
    }
}
