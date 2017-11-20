package com.kikatech.voice.core.dialogflow.scene;

/**
 * @author SkeeterWang Created on 2017/11/14.
 */
public interface IDialogFlowFeedback {
    interface IToSceneFeedback {
        void onTtsStart();

        void onTtsComplete();

        void onTtsError();

        void onTtsInterrupted();

        boolean isEndOfScene();
    }
}
