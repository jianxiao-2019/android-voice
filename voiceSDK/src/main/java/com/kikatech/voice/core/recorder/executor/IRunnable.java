package com.kikatech.voice.core.recorder.executor;

/**
 * @author SkeeterWang Created on 2018/6/4.
 */

public interface IRunnable extends Runnable {
    void cancel();

    boolean isRunning();
}
