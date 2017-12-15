package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;
import android.util.Pair;

/**
 * Created by tianli on 17-11-11.
 */

public interface ISceneFeedback {
    void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras, ISceneStageFeedback feedback);

    void onText(String text, Bundle extras, ISceneStageFeedback feedback);

    void onStagePrepared(String scene, String action, SceneStage sceneStage);

    void onStageActionStart();

    void onStageActionDone(boolean isInterrupted);

    void onStageEvent(Bundle extras);
}