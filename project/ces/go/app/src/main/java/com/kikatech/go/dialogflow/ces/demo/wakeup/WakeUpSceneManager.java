package com.kikatech.go.dialogflow.ces.demo.wakeup;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * @author SkeeterWang Created on 2018/1/2.
 */

public class WakeUpSceneManager extends BaseSceneManager {

    public WakeUpSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneWakeUp(mContext, mService.getTtsFeedback()));
    }
}
