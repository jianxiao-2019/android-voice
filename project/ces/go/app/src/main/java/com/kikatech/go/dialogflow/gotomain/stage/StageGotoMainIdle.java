package com.kikatech.go.dialogflow.gotomain.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/12/28.
 */

public class StageGotoMainIdle extends BaseSceneStage {
    public StageGotoMainIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if(LogUtil.DEBUG) {
            LogUtil.log(TAG, "action:" + action + ", extra:" + extra);
        }
        return new StageGotoMain(mSceneBase, mFeedback);
    }

    @Override
    protected void prepare() {

    }

    @Override
    protected void action() {

    }
}
