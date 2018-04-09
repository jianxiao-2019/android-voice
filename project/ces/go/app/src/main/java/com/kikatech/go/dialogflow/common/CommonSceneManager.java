package com.kikatech.go.dialogflow.common;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;

/**
 * @author SkeeterWang Created on 2017/11/29.
 */

public class CommonSceneManager extends BaseSceneManager {

    public CommonSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneCommon(mContext, mService.getTtsFeedback()));
    }
}