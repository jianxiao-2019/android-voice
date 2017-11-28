package com.kikatech.voice.service;

import android.os.Bundle;
import android.util.Pair;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/7.
 */

public interface IDialogFlowService {

    interface IServiceCallback {

        void onInitComplete();

        void onASRResult(String speechText, boolean isFinished);

        void onText(String text, Bundle extras);

        void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras);

        void onStagePrepared(String scene, String action, SceneStage sceneStage);

        void onStageActionDone(boolean isInterrupted);

        void onStageEvent(Bundle extras);

        void onSceneExit();
    }

    interface IAgentQueryStatus {
        void onStart();

        void onComplete(String[] dbgMsg);

        void onError(Exception e);
    }

    void registerScene(SceneBase scene);

    void unregisterScene(SceneBase scene);

    ISceneFeedback getTtsFeedback();

    void resetContexts();

    void talk(final String words);

    void text(final String words);

    void quitService();
}