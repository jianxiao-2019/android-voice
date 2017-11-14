package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by tianli on 17-11-11.
 */

public abstract class SceneStage {

    public final static String TAG = "SceneStage";
    protected ISceneFeedback mFeedback;

    protected SceneBase mSceneBase = null;

    public SceneStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        mFeedback = feedback;
        mSceneBase = scene;
    }

    /**
     * Move to next stage according to the action from agent
     *
     * @param action the action from agent
     * @param extra  extra parameters
     */
    public abstract SceneStage next(String action, Bundle extra);

    /**
     * Perform the action, must be invoked after call {@link #next(String, Bundle) next}
     */
    public abstract void action();

    final protected void exitScene() {
        mSceneBase.exit();
    }

    protected void speak(String text) {
        if (mFeedback != null) {
            mFeedback.onText(text);
        }
    }
}


