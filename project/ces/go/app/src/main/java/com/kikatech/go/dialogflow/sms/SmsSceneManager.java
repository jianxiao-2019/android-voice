package com.kikatech.go.dialogflow.sms;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneManager;
import com.kikatech.voice.service.IDialogFlowService;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SmsSceneManager extends BaseSceneManager {

    public SmsSceneManager(Context context, @NonNull IDialogFlowService service) {
        super(context, service);
    }

    @Override
    protected void initScenes() {
        mSceneBaseList.add(new SceneSendSms(mContext, mService.getTtsFeedback()));
    }
}
