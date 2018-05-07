package com.kikatech.go.dialogflow.close;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;

/**
 * @author SkeeterWang Created on 2018/5/7.
 */

public class CloseSceneManager extends BaseSceneManager {

    public CloseSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneClose(mContext, mService.getTtsFeedback()));
    }
}