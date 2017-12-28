package com.kikatech.go.dialogflow.gotomain;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by brad_chang on 2017/12/28.
 */

public class GotoMainSceneManager extends BaseSceneManager {
    public GotoMainSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneGotoMain(mContext, mService.getTtsFeedback()));
    }
}
