package com.kikatech.go.dialogflow.navigation;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by bradchang on 2017/11/13.
 */

public class NaviSceneManager extends BaseSceneManager {

    public NaviSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneNavigation(mContext, mService.getTtsFeedback()));
    }
}
