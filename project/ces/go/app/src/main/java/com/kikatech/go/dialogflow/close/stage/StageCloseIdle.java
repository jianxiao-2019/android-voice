package com.kikatech.go.dialogflow.close.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.close.SceneCloseActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/7.
 */

public class StageCloseIdle extends BaseCloseStage {
    private static final String TAG = "StageCloseIdle";

    public StageCloseIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("action: %s, extra: %s", action, extra));
        }
        switch (action) {
            case SceneCloseActions.ACTION_CLOSE_START:
                return new StageCloseStart(mSceneBase, mFeedback);
        }
        return this;
    }
}