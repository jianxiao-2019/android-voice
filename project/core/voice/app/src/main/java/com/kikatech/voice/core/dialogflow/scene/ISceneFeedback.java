package com.kikatech.voice.core.dialogflow.scene;

/**
 * Created by tianli on 17-11-11.
 */

public interface ISceneFeedback {
    void onText(String text, IDialogFlowFeedback.IToSceneFeedback feedback);
}
