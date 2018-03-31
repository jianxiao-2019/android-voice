package com.kikatech.go.dialogflow.error;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * @author SkeeterWang Created on 2018/3/31.
 */

public class ErrorSceneManager extends BaseSceneManager {

    public ErrorSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneError(mContext, mService.getTtsFeedback()));
    }
}
