package com.kikatech.go.dialogflow.help.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.help.SceneHelpActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/2.
 */

public class StageHelpIdle extends BaseHelpStage {

    public StageHelpIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("action: %s, extra: %s", action, extra));
        }
        switch (action) {
            case SceneHelpActions.ACTION_HELP_START:
                return new StageHelpStart(mSceneBase, mFeedback);
        }
        return this;
    }
}
