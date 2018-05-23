package com.kikatech.voice.service.dialogflow;

import android.os.Bundle;
import android.util.Pair;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;

/**
 * Created by bradchang on 2017/11/7.
 */

public interface IDialogFlowService extends IDialogFlowVoiceService {

    byte QUERY_TYPE_SERVER = 1;
    byte QUERY_TYPE_LOCAL = 2;
    byte QUERY_TYPE_EMOJI = 3;

    interface IServiceCallback {

        void onInitComplete();

        void onWakeUp(String scene);

        void onSleep();

        void onASRPause();

        void onASRResume();

        void onASRResult(String speechText, String emojiUnicode, boolean isFinished);

        void onError(int reason);

        void onText(String text, Bundle extras);

        void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras);

        void onStagePrepared(String scene, String action, SceneStage sceneStage);

        void onStageActionStart(boolean supportAsrInterrupted);

        void onStageActionDone(boolean isInterrupted, Integer overrideAsrBos);

        void onStageEvent(Bundle extras);

        void onSceneExit(boolean proactive);

        void onAsrConfigChange(AsrConfiguration asrConfig);

        void onRecorderSourceUpdate();
    }

    interface IAgentQueryStatus {
        void onStart(boolean proactive);

        void onComplete(String[] dbgMsg);

        void onError(Exception e);
    }

    interface ITtsStatusCallback {
        void onStart();

        void onStop();
    }

    void init();

    /**
     * @param volume range 0.0 to 1.0
     */
    void setTtsVolume(float volume);

    void registerScene(SceneBase scene);

    void unregisterScene(SceneBase scene);

    void resetContexts();

    void talk(final String words, boolean proactive);

    void onLocalIntent(final String scene, final String action);

    void talkUncaught();

    ISceneFeedback getTtsFeedback();

    void quitService();

    void updateRecorderSource(VoiceConfiguration config);
}