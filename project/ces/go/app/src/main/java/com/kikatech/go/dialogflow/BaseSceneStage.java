package com.kikatech.go.dialogflow;

import android.support.annotation.NonNull;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/11/15.
 */

public abstract class BaseSceneStage extends SceneStage {

    public static final String EXTRA_OPTIONS_LIST = "extra_options_list";

    public BaseSceneStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }
}
