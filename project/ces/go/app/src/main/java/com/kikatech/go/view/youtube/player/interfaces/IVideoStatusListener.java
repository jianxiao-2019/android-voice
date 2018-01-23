package com.kikatech.go.view.youtube.player.interfaces;

/**
 * @author SkeeterWang Created on 2018/1/19.
 */

public interface IVideoStatusListener {

    void onFirstVideoFrameRendered();

    void onPlay();

    void onPause();

    void onBuffer();

    boolean onStopWithExternalError(int position);
}
