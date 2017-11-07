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
import com.kikatech.voice.util.log.Logger;

import java.util.List;


/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowService implements
        IDialogFlowService, DialogObserver,
        VoiceService.VoiceRecognitionListener, VoiceService.VoiceStateChangedListener {

    private static final String TAG = "DialogFlowService";

    private final IServiceCallback mCallback;
    private VoiceService mVoiceService;
    private DialogFlow mDialogFlow;


    private DialogFlowService(@NonNull Context ctx, @NonNull VoiceConfiguration conf, @NonNull IServiceCallback callback) {
        mCallback = callback;

        initDialogFlow(ctx, conf);

        initVoiceService(ctx, conf);

        callback.onInitComplete();
    }

    private void initDialogFlow(@NonNull Context ctx, @NonNull VoiceConfiguration conf) {
        mDialogFlow = new DialogFlow(ctx, conf);

        registerScenes();

        if(LogUtil.DEBUG) LogUtil.log(TAG, "init DialogFlow ... Done");
    }

    private void initVoiceService(@NonNull Context ctx, @NonNull VoiceConfiguration conf) {
        mVoiceService = VoiceService.getService(ctx, conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.start();
        if(LogUtil.DEBUG) LogUtil.log(TAG, "init VoiceService ... Done");
    }

    private void debugDumpIntent(Intent intent) {
        if (LogUtil.DEBUG && intent != null) {
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
    }

    private void registerScenes() {
        // 0. Common flow / Error handling
        mDialogFlow.register(Scene.DEFAULT.toString(), this);

        // 1. Navigation
        mDialogFlow.register(Scene.NAVIGATION.toString(), new SceneNavigation(new SceneBase.ISceneCallback() {
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
        }));


        // Debug
        if (LogUtil.DEBUG) {
            DialogObserver debugLogger = new DialogObserver() {
                @Override
                public void onIntent(Intent intent) {
                    debugDumpIntent(intent);
                }
            };
            mDialogFlow.register(Scene.DEFAULT.toString(), debugLogger);
            mDialogFlow.register(Scene.NAVIGATION.toString(), debugLogger);
        }

        mDialogFlow.resetContexts();
    }

    public static synchronized DialogFlowService queryService(@NonNull Context ctx, @NonNull VoiceConfiguration conf, @NonNull IServiceCallback callback) {
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
            if(LogUtil.DEBUG) LogUtil.log(TAG, "talk : " + words);
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
        Logger.i("onMessage message = " + message.text);
        if(message.seqId < 0 && !TextUtils.isEmpty(message.text)) {
            if(LogUtil.DEBUG) LogUtil.log(TAG, "Speech spoken : " + message.text);

            if(mDialogFlow != null) {
                mDialogFlow.talk(message.text);
            }

            if(mCallback != null) {
                mCallback.onSpeechSpokenDone(message.text);
            }
        }
    }

    @Override
    public void onStartListening() {
        if(LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceService] onStartListening");
    }

    @Override
    public void onStopListening() {
        if(LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceService] onStopListening");
    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {
        if(LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceService] onSpeechProbabilityChanged:" + prob);
    }

    @Override
    public void onIntent(Intent intent) {
        // Process Default Fallback Intent
        List<DialogObserver> subs = mDialogFlow.getListeningSubscribers();
    }
}