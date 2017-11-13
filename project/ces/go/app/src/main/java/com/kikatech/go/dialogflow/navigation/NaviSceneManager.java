package com.kikatech.go.dialogflow.navigation;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by bradchang on 2017/11/13.
 */

public class NaviSceneManager extends BaseSceneManager {

    private SceneNavigation mSceneNav;

    public NaviSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void registerScenes() {
        mService.registerScene(mSceneNav = new SceneNavigation(mContext, mService.getTtsFeedback()));
    }

    @Override
    protected void unregisterScenes() {
        mService.unregisterScene(mSceneNav);
    }

    @Override
    public void close() {
        unregisterScenes();
    }
}
