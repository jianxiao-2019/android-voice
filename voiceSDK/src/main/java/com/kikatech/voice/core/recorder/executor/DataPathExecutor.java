package com.kikatech.voice.core.recorder.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SkeeterWang Created on 2018/6/5.
 */

public class DataPathExecutor extends BaseExecutor {
    private static final String TAG = "DataPathExecutor";

    public DataPathExecutor() {
        super();
    }

    @Override
    protected boolean isLogAvailable() {
        return false;
    }

    @Override
    protected ExecutorService getExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
