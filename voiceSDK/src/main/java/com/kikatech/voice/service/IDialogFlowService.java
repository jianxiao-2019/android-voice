package com.kikatech.voice.service;

import android.os.Bundle;
import android.util.Pair;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.conf.AsrConfiguration;

/**
 * Created by bradchang on 2017/11/7.
 */

public interface IDialogFlowService {

    byte QUERY_TYPE_SERVER = 1;
    byte QUERY_TYPE_LOCAL = 2;
    byte QUERY_TYPE_EMOJI = 3;

    interface IServiceCallback {

        byte CONNECTION_STATUS_OPENED = 1;
        byte CONNECTION_STATUS_CLOSED = 2;
        byte CONNECTION_STATUS_ERR_DISCONNECT = 3;

        void onInitComplete();

        void onWakeUp(String scene);

        void onSleep();

        void onVadBos();

        void onVadEos(boolean hasIntermediateResult);

        void onASRPause();

        void onASRResume();

        void onASRResult(String speechText, String emojiUnicode, boolean isFinished);

        void onText(String text, Bundle extras);

        void onTextPairs(Pair<String, Integer>[] pairs, Bundle extras);

        void onStagePrepared(String scene, String action, SceneStage sceneStage);

        void onStageActionStart(boolean supportAsrInterrupted);

        void onStageActionDone(boolean isInterrupted, boolean delayAsrResume, Integer overrideAsrBos);

        void onStageEvent(Bundle extras);

        void onSceneExit(boolean proactive);

        void onAsrConfigChange(AsrConfiguration asrConfig);

        void onRecorderSourceUpdate();

        void onConnectionStatusChange(byte status);
    }

    interface IAgentQueryStatus {
        void onStart(boolean proactive);

        void onComplete(String[] dbgMsg);

        void onError(Exception e);
    }

    void registerScene(SceneBase scene);

    void unregisterScene(SceneBase scene);

    ISceneFeedback getTtsFeedback();

    void wakeUp(String wakeupFrom);

    void sleep();

    void resetContexts();

    void talk(final String words, boolean proactive);

    void onLocalIntent(final String scene, final String action);

    void talkUncaught();

    void pauseAsr();

    void forceArsResult();

    void resumeAsr(int bosDuration);

    void resumeAsr(boolean startBosNow);

    void cancelAsrAlignment();

    void quitService();

    void updateRecorderSource(VoiceConfiguration config);

    void updateTtsSource(VoiceConfiguration config);

    void setWakeUpDetectorEnable(boolean enable);

    boolean isWakeUpDetectorEnabled();
}