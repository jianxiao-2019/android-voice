package com.kikatech.voice.service;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.SceneType;

/**
 * Created by bradchang on 2017/11/7.
 */

public interface IDialogFlowService {

    interface IServiceCallback {

        void onInitComplete();

        // Please do your task in your own thread
        void onCommand(SceneType scene, byte cmd, Bundle parameters);

        void onSpeechSpokenDone(String speechText);
    }

    void resetContexts();

    void talk(final String words);

    void quitService();
}
