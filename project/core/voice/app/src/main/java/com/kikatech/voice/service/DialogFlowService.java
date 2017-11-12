package com.kikatech.voice.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.kikatech.voice.core.dialogflow.DialogFlow;
import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.tts.TtsService;
import com.kikatech.voice.core.tts.TtsSpeaker;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.util.log.LogUtil;


/**
 * Created by bradchang on 2017/11/7.
 */

public class DialogFlowService implements
        IDialogFlowService,
        VoiceService.VoiceRecognitionListener, VoiceService.VoiceStateChangedListener {

    private static final String TAG = "DialogFlowService";

    private Context mContext;

    private final IServiceCallback mCallback;
    private VoiceService mVoiceService;
    private DialogFlow mDialogFlow;
    private TtsSpeaker mTtsSpeaker;

//    private SceneNavigationOld mSceneNavigation;
//    private SceneTelephonyIncoming mSceneTelephonyIncoming;
//    private SceneTelephonyOutgoing mSceneTelephonyOutgoing;
//    private DialogObserver debugLogger;

    private DialogFlowService(@NonNull Context ctx, @NonNull VoiceConfiguration conf, @NonNull IServiceCallback callback) {
        mContext = ctx;
        mCallback = callback;
        initDialogFlow(conf);
        initVoiceService(conf);
        initTts();
        callback.onInitComplete();
    }

    public void registerScene(String scene, DialogObserver observer) {
        mDialogFlow.register(scene, observer);
    }

    public void unregisterScene(String scene, DialogObserver observer){
        mDialogFlow.unregister(scene, observer);
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

    private void initTts() {
        if (mTtsSpeaker == null) {
            mTtsSpeaker = TtsService.getInstance().getSpeaker();
            mTtsSpeaker.init(mContext, null);
            mTtsSpeaker.setTtsStateChangedListener(mTtsListener);
        }
    }

    private void tts(String words) {
        if (mTtsSpeaker == null) {
            return;
        }
        try {
            if(mTtsSpeaker != null) {
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, "tts, words: " + words);
                }
                mTtsSpeaker.speak(words);
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, "tts, words: " + words);
                }
                mTtsSpeaker.speak(words);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            toast(words);
        }
    }

    private void toast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            }
        });
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
//         1. Navigation
//        mSceneNavigation = new SceneNavigationOld(new SceneBaseOld.ISceneCallback() {
//            @Override
//            public void resetContextImpl() {
//                mDialogFlow.resetContexts();
//            }
//
//            @Override
//            public void onCommand(byte cmd, Bundle parameters) {
//                if (mCallback != null) {
//                    mCallback.onCommand(SceneType.NAVIGATION, cmd, parameters);
//                }
//            }
//        });
//        mDialogFlow.register(SceneType.NAVIGATION.toString(), mSceneNavigation);
        // 2. Telephony Incoming
//        mSceneTelephonyIncoming = new SceneTelephonyIncoming(mContext, new SceneBaseOld.ISceneCallback() {
//            @Override
//            public void resetContextImpl() {
//                mDialogFlow.resetContexts();
//            }
//            @Override
//            public void onCommand(byte cmd, Bundle parameters) {
//                switch (cmd) {
//                    case TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_PRE_START:
//                        String phoneNumber = parameters.getString(TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_NAME);
//                        startTelephonyIncoming(phoneNumber);
//                        break;
//                    default:
//                        if (mCallback != null) {
//                            mCallback.onCommand(SceneType.TELEPHONY_INCOMING, cmd, parameters);
//                        }
//                        break;
//                }
//            }
//        });
//        mDialogFlow.register(SceneType.TELEPHONY_INCOMING.toString(), mSceneTelephonyIncoming);
//        mDialogFlow.register(SceneType.DEFAULT.toString(), mSceneTelephonyIncoming);
        // 3. Telephony Outgoing
//        mSceneTelephonyOutgoing = new SceneTelephonyOutgoing(mContext, new SceneBaseOld.ISceneCallback() {
//            @Override
//            public void resetContextImpl() {
//                mDialogFlow.resetContexts();
//            }
//            @Override
//            public void onCommand(byte cmd, Bundle parameters) {
//                if (mCallback != null) {
//                    mCallback.onCommand(SceneType.TELEPHONY_OUTGOING, cmd, parameters);
//                }
//            }
//        });
//        mDialogFlow.register(SceneType.TELEPHONY_OUTGOING.toString(), mSceneTelephonyOutgoing);
        // Debug
//        if (LogUtil.DEBUG) {
//            debugLogger = new DialogObserver() {
//                @Override
//                public void onIntent(Intent intent) {
//                    debugDumpIntent(intent);
//                }
//            };
//            mDialogFlow.register(SceneType.DEFAULT.toString(), debugLogger);
//            mDialogFlow.register(SceneType.NAVIGATION.toString(), debugLogger);
//        }

        mDialogFlow.resetContexts();
    }

//    private void startTelephonyIncoming(String phoneNumber) {
//        String cmdIntoTelephonyIntent = String.format(SceneTelephonyIncoming.KIKA_PROCESS_INCOMING_CALL, phoneNumber);
//        mDialogFlow.talk(cmdIntoTelephonyIntent);
//    }

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
        if (mVoiceService != null) {
            mVoiceService.stop();
        }
        if (mTtsSpeaker != null) {
            mTtsSpeaker.close();
            mTtsSpeaker = null;
        }
        if (LogUtil.DEBUG) {
//            mDialogFlow.unregister(SceneType.DEFAULT.toString(), debugLogger);
//            mDialogFlow.unregister(SceneType.NAVIGATION.toString(), debugLogger);
        }
//        try {
//            mSceneTelephonyIncoming.unregisterBroadcastReceiver(mContext);
//        } catch (Exception ignore) {
//        }
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

    public ISceneFeedback getTtsFeedback() {
        return mSceneFeedback;
    }

    private ISceneFeedback mSceneFeedback = new ISceneFeedback() {
        @Override
        public void onText(String text) {
            tts(text);
        }
    };

    private TtsSpeaker.TtsStateChangedListener mTtsListener = new TtsSpeaker.TtsStateChangedListener() {
        @Override
        public void onTtsStart() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsStart");
            }
        }

        @Override
        public void onTtsComplete() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsComplete");
            }
        }

        @Override
        public void onTtsInterrupted() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsInterrupted");
            }
        }

        @Override
        public void onTtsError() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTtsError");
            }
        }
    };

//    @Override
//    public void onIntent(Intent intent) {
    // Process Default Fallback Intent
    // List<DialogObserver> subs = mDialogFlow.getListeningSubscribers();
    // TODO: 17-11-10 GENERAL_CMD_UNKNOWN
//        mCallback.onCommand(SceneType.DEFAULT, GeneralCommand.GENERAL_CMD_UNKNOWN, null);
//    }
}