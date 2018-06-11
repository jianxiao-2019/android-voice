package com.kikatech.voice.core.recorder.executor;

import com.kikatech.voice.util.log.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SkeeterWang Created on 2018/6/4.
 */

public class AudioRecordExecutor extends BaseExecutor {
    private static final String TAG = "AudioRecordExecutor";

    public AudioRecordExecutor() {
        super();
    }

    @Override
    protected boolean isLogAvailable() {
        return Logger.DEBUG;
    }

    @Override
    protected ExecutorService getExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
