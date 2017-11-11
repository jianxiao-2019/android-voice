package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;

/**
 * Created by tianli on 17-11-11.
 */

public abstract class SceneStage {

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

    public static class Idle extends SceneStage {

        @Override
        public void action() {
        }

        @Override
        public SceneStage next(String action, Bundle extra) {
            return this;
        }
    }
}


