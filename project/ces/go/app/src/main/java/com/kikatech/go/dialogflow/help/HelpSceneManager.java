package com.kikatech.go.dialogflow.help;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;

/**
 * @author SkeeterWang Created on 2018/5/2.
 */

public class HelpSceneManager extends BaseSceneManager {

    public HelpSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneHelp(mContext, mService.getTtsFeedback()));
    }
}
