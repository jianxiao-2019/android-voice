package com.kikatech.voice.service;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogFlow;
import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.constant.Scene;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneNavigation;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.util.log.LogUtil;


/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowService implements
        IDialogFlowService, VoiceService.VoiceRecognitionListener, VoiceService.VoiceStateChangedListener {

    private static final String TAG = "DialogFlowService";

    private final VoiceService mVoiceService;
    private DialogFlow mDialogFlow;
    private final ICommandCallback mCallback;
    private DialogObserver mDialogObserver;


    private DialogFlowService(@NonNull Context ctx, @NonNull VoiceConfiguration conf, @NonNull ICommandCallback callback) {
        initDialogFlow(ctx, conf);

        mCallback = callback;

        mVoiceService = VoiceService.getService(ctx, conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.start();
    }

    private void initDialogFlow(@NonNull Context ctx, @NonNull VoiceConfiguration conf) {
        mDialogFlow = new DialogFlow(ctx, conf);

        mDialogObserver = new DialogObserver() {
            @Override
            public void onIntent(Intent intent) {
                if (intent != null) {
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "===== START ON INTENT =====");
                        LogUtil.log(TAG, "Scene: " + intent.getScene());
                        LogUtil.log(TAG, "Name: " + intent.getName());
                        LogUtil.log(TAG, "Action: " + intent.getAction());
                        Bundle args = intent.getExtra();
                        if (args != null && !args.isEmpty()) {
                            for (String key : args.keySet()) {
                                LogUtil.log(TAG, "[params] " + key + ": " + args.getString(key));
                            }
                        }
                        LogUtil.log(TAG, "===== STOP ON INTENT =====");
                    }
                    //handleLog(intent);
                }
            }
        };

        registerScenes();
    }

    private void registerScenes() {
        final SceneNavigation sn = new SceneNavigation(new SceneBase.ISceneCallback() {
            @Override
            public void resetContextImpl() {
                mDialogFlow.resetContexts();
            }

            @Override
            public void onCommand(byte cmd, Bundle parameters) {
                if (mCallback != null) {
                    mCallback.onCommand(Scene.NAVIGATION, cmd, parameters);
                }
            }
        });

        // Scenario Demo
        mDialogFlow.register(Scene.NAVIGATION.toString(), sn);
        mDialogFlow.register(Scene.DEFAULT.toString(), sn);

        // Debug
        mDialogFlow.register(Scene.DEFAULT.toString(), mDialogObserver);
        mDialogFlow.register(Scene.NAVIGATION.toString(), mDialogObserver);

        mDialogFlow.resetContexts();
    }

    public static synchronized DialogFlowService queryService(@NonNull Context ctx, @NonNull VoiceConfiguration conf, @NonNull ICommandCallback callback) {
        return new DialogFlowService(ctx, conf, callback);
    }

    @Override
    public void resetContexts() {
        if (mDialogFlow != null) {
            mDialogFlow.resetContexts();
        }
    }

    @Override
    public void talk(String words) {
        if(mDialogFlow != null && !TextUtils.isEmpty(words)) {
            mDialogFlow.talk(words);
        }
    }

    @Override
    public void quitService() {
        // TODO stop all services
        if (mVoiceService != null) {
            mVoiceService.stop();
        }
    }

    @Override
    public void onRecognitionResult(Message message) {
        LogUtil.log(TAG, "onMessage message = " + message.text);
    }

    @Override
    public void onStartListening() {

    }

    @Override
    public void onStopListening() {

    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {

    }
}