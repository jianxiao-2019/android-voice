package com.kikatech.go.view.youtube.interfaces;

/**
 * @author SkeeterWang Created on 2017/6/20.
 */
public interface VideoStatusListener {
    void onFirstVideoFrameRendered();

    void onPlay();

    void onPause();

    void onBuffer();

    boolean onStopWithExternalError(int position);
}
