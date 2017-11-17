package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SmsSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageSendSmsIdle extends BaseSendSmsStage {

    public StageSendSmsIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        if (!action.equals(SmsSceneActions.ACTION_SEND_SMS)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
            return null;
        }

        return checkSendTargetAvailable(TAG, getSmsContent(), mSceneBase, mFeedback);
    }
}