package com.kikatech.go.tutorial.dialogflow;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;

/**
 * @author SkeeterWang Created on 2018/5/10.
 */

public class TutorialSceneManager extends BaseSceneManager {

    public TutorialSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneTutorial(mContext, mService.getTtsFeedback()));
    }
}