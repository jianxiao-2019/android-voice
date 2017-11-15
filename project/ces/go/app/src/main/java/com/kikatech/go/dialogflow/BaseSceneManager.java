package com.kikatech.go.dialogflow;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.service.IDialogFlowService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bradchang on 2017/11/13.
 */

public abstract class BaseSceneManager {

    protected IDialogFlowService mService;
    protected Context mContext;
    protected final List<SceneBase> mSceneBaseList = new ArrayList<>();

    public BaseSceneManager(Context context, @NonNull IDialogFlowService service) {
        mContext = context.getApplicationContext();
        mService = service;

        initScenes();
        registerScenes();
    }

    protected abstract void initScenes();

    private void registerScenes() {
        for(SceneBase sb : mSceneBaseList) {
            mService.registerScene(sb);
        }
    }

    private void unregisterScenes() {
        for(SceneBase sb : mSceneBaseList) {
            mService.unregisterScene(sb);
        }
        mSceneBaseList.clear();
    }

    public void close() {
        unregisterScenes();
    }
}
