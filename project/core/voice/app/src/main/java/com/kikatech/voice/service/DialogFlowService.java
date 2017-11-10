package com.kikatech.voice.service;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogFlow;
import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.constant.GeneralCommand;
import com.kikatech.voice.core.dialogflow.constant.Scene;
import com.kikatech.voice.core.dialogflow.constant.TelephonyIncomingCommand;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneNavigation;
import com.kikatech.voice.core.dialogflow.scene.SceneTelephonyIncoming;
import com.kikatech.voice.core.dialogflow.scene.SceneTelephonyOutgoing;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.util.log.LogUtil;



/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowService implements
        IDialogFlowService, DialogObserver,
        VoiceService.VoiceRecognitionListener, VoiceService.VoiceStateChangedListener {

    private static final String TAG = "DialogFlowService";

    private Context mContext;

    private final IServiceCallback mCallback;
    private VoiceService mVoiceService;
    private DialogFlow mDialogFlow;

    private SceneNavigation mSceneNavigation;
    private SceneTelephonyIncoming mSceneTelephonyIncoming;
    private SceneTelephonyOutgoing mSceneTelephonyOutgoing;
    private DialogObserver debugLogger;

    private DialogFlowService(@NonNull Context ctx, @NonNull VoiceConfiguration conf, @NonNull IServiceCallback callback) {
        mContext = ctx;

        mCallback = callback;

        initDialogFlow(conf);

        initVoiceService(conf);

        callback.onInitComplete();
    }

    private void initDialogFlow(@NonNull VoiceConfiguration conf) {
        mDialogFlow = new DialogFlow(mContext, conf);

        registerScenes();

        if (LogUtil.DEBUG) LogUtil.log(TAG, "init DialogFlow ... Done");
    }

    private void initVoiceService(@NonNull VoiceConfiguration conf) {
        mVoiceService = VoiceService.getService(mContext, conf);
        mVoiceService.setVoiceRecognitionListener(this);
        mVoiceService.setVoiceStateChangedListener(this);
        mVoiceService.start();
        if (LogUtil.DEBUG) LogUtil.log(TAG, "init VoiceService ... Done");
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
        mSceneNavigation = new SceneNavigation(new SceneBase.ISceneCallback() {
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
        mDialogFlow.register(Scene.NAVIGATION.toString(), mSceneNavigation);

        // 2. Telephony Incoming
        mSceneTelephonyIncoming = new SceneTelephonyIncoming(mContext, new SceneBase.ISceneCallback() {
            @Override
            public void resetContextImpl() {
                mDialogFlow.resetContexts();
            }

            @Override
            public void onCommand(byte cmd, Bundle parameters) {
                switch (cmd) {
                    case TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_PRE_START:
                        String phoneNumber = parameters.getString(TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_NAME);
                        startTelephonyIncoming(phoneNumber);
                        break;
                    default:
                        if (mCallback != null) {
                            mCallback.onCommand(Scene.TELEPHONY_INCOMING, cmd, parameters);
                        }
                        break;
                }
            }
        });
        mDialogFlow.register(Scene.TELEPHONY_INCOMING.toString(), mSceneTelephonyIncoming);
        mDialogFlow.register(Scene.DEFAULT.toString(), mSceneTelephonyIncoming);

        // 3. Telephony Outgoing
        mSceneTelephonyOutgoing = new SceneTelephonyOutgoing(mContext, new SceneBase.ISceneCallback() {
            @Override
            public void resetContextImpl() {
                mDialogFlow.resetContexts();
            }

            @Override
            public void onCommand(byte cmd, Bundle parameters) {
                if (mCallback != null) {
                    mCallback.onCommand(Scene.TELEPHONY_OUTGOING, cmd, parameters);
                }
            }
        });
        mDialogFlow.register(Scene.TELEPHONY_OUTGOING.toString(), mSceneTelephonyOutgoing);

        // Debug
        if (LogUtil.DEBUG) {
            debugLogger = new DialogObserver() {
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

    private void startTelephonyIncoming(String phoneNumber) {
        String cmdIntoTelephonyIntent = String.format(SceneTelephonyIncoming.KIKA_PROCESS_INCOMING_CALL, phoneNumber);
        mDialogFlow.talk(cmdIntoTelephonyIntent);
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
        if (mDialogFlow != null && !TextUtils.isEmpty(words)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "talk : " + words);
            mDialogFlow.talk(words);
        }
    }

    @Override
    public void quitService() {
        // TODO stop all services
        if (mVoiceService != null) {
            mVoiceService.stop();
        }
        try {
            mDialogFlow.unregister(Scene.NAVIGATION.toString(), mSceneNavigation);
            mSceneTelephonyIncoming.unregisterBroadcastReceiver(mContext);
            mDialogFlow.unregister(Scene.TELEPHONY_INCOMING.toString(), mSceneTelephonyIncoming);
            mDialogFlow.unregister(Scene.DEFAULT.toString(), mSceneTelephonyIncoming);
            if (LogUtil.DEBUG) {
                mDialogFlow.unregister(Scene.DEFAULT.toString(), debugLogger);
                mDialogFlow.unregister(Scene.NAVIGATION.toString(), debugLogger);
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onRecognitionResult(Message message) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "onMessage message = " + message.text);
        if (message.seqId < 0 && !TextUtils.isEmpty(message.text)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Speech spoken : " + message.text);

            if (mDialogFlow != null) {
                mDialogFlow.talk(message.text);
            }

            if (mCallback != null) {
                mCallback.onSpeechSpokenDone(message.text);
            }
        }
    }

    @Override
    public void onStartListening() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceService] onStartListening");
    }

    @Override
    public void onStopListening() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceService] onStopListening");
    }

    @Override
    public void onSpeechProbabilityChanged(float prob) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "[VoiceService] onSpeechProbabilityChanged:" + prob);
    }

    @Override
    public void onIntent(Intent intent) {
        // Process Default Fallback Intent
        // List<DialogObserver> subs = mDialogFlow.getListeningSubscribers();
        mCallback.onCommand(Scene.DEFAULT, GeneralCommand.GENERAL_CMD_UNKNOWN, null);
    }
}