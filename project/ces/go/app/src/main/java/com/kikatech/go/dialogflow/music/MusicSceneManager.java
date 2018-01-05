package com.kikatech.go.dialogflow.music;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class MusicSceneManager extends BaseSceneManager {

    public MusicSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneMusic(mContext, mService.getTtsFeedback()));
    }
}
