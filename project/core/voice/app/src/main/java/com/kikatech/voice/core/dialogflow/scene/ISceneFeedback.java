package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;
import android.util.Pair;

/**
 * Created by tianli on 17-11-11.
 */

public interface ISceneFeedback {
    void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras, IDialogFlowFeedback.IToSceneFeedback feedback);

    void onText(String text, Bundle extras, IDialogFlowFeedback.IToSceneFeedback feedback);

    void onStagePrepared(String scene, String action, SceneStage sceneStage);

    void onStageActionDone(boolean isEndOfScene, boolean isInterrupted);
}