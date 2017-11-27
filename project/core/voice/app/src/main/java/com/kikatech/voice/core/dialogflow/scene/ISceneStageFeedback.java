package com.kikatech.voice.core.dialogflow.scene;

/**
 * @author SkeeterWang Created on 2017/11/27.
 */

public interface ISceneStageFeedback {
    void onStageActionStart();

    void onStageActionDone(boolean isInterrupted);
}
