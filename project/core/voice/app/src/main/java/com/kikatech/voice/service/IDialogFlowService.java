package com.kikatech.voice.service;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by bradchang on 2017/11/7.
 */

public interface IDialogFlowService {

    interface IServiceCallback {

        void onInitComplete();

        void onSpeechSpokenDone(String speechText);
    }

    void registerScene(SceneBase scene);

    void unregisterScene(SceneBase scene);

    ISceneFeedback getTtsFeedback();

    void resetContexts();

    void talk(final String words);

    void quitService();
}
