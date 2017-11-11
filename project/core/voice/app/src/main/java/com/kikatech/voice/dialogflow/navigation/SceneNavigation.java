package com.kikatech.voice.dialogflow.navigation;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class SceneNavigation extends SceneBase {

    public static final String SCENE = "Navigation";

    @Override
    protected SceneStage init() {
        return new SceneStage() {
            @Override
            public SceneStage next(String action, Bundle extra) {
                return null;
            }

            @Override
            public void action() {
            }
        };
    }
}
