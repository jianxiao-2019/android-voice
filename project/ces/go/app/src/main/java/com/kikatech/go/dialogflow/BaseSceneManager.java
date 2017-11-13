package com.kikatech.go.dialogflow;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by bradchang on 2017/11/13.
 */

public abstract class BaseSceneManager {

    protected IDialogFlowService mService;
    protected Context mContext;

    public BaseSceneManager(Context context, @NonNull IDialogFlowService service) {
        mContext = context.getApplicationContext();
        mService = service;

        registerScenes();
    }

    protected abstract void registerScenes();

    protected abstract void unregisterScenes();

    public abstract void close();
}
