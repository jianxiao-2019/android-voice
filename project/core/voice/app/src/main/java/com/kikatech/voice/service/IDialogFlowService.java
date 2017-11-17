package com.kikatech.voice.service;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/7.
 */

public interface IDialogFlowService {

    interface IServiceCallback {

        void onInitComplete();

        void onSpeechSpokenDone(String speechText);

        void onText(String text, Bundle extras);

        void onSceneExit(String scene);

        void onStagePrepared(String scene, String action, SceneStage sceneStage);

        void onStageActionDone();
    }

    void registerScene(SceneBase scene);

    void unregisterScene(SceneBase scene);

    ISceneFeedback getTtsFeedback();

    void resetContexts();

    void talk(final String words);

    void text(final String words);

    void quitService();
}
