package com.kikatech.go.dialogflow.im;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.go.dialogflow.im.reply.SceneReplyIM;
import com.kikatech.go.dialogflow.im.send.SceneSendIM;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class IMSceneManager extends BaseSceneManager {

    public IMSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneSendIM(mContext, mService.getTtsFeedback()));
        mSceneBaseList.add(new SceneReplyIM(mContext, mService.getTtsFeedback()));
    }
}
