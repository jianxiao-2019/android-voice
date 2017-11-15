package com.kikatech.go.dialogflow.stop;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by bradchang on 2017/11/14.
 */

public class SceneStopIntentManager extends BaseSceneManager {

    public SceneStopIntentManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneStopIntent(mContext, mService.getTtsFeedback()));
    }
}
