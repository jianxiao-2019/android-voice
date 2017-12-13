package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageSendIMIdle extends BaseSendIMStage {
    public StageSendIMIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected @AsrConfigUtil.ASRMode int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_COMMAND;
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        return getCheckIMAppStage(TAG, getIMContent(), mSceneBase, mFeedback);
    }
}
