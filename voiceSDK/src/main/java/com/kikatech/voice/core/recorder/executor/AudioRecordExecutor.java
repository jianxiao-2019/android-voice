package com.kikatech.voice.core.recorder.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SkeeterWang Created on 2018/6/4.
 */

public class AudioRecordExecutor extends BaseExecutor {
    private static final String TAG = "AudioRecordExecutor";

    private static AudioRecordExecutor sIns;

    public static synchronized AudioRecordExecutor getIns() {
        if (sIns == null) {
            sIns = new AudioRecordExecutor();
        }
        return sIns;
    }

    private AudioRecordExecutor() {
        super();
    }

    @Override
    protected ExecutorService getExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
