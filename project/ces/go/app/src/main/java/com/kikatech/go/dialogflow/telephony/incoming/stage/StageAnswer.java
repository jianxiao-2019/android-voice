package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageAnswer extends SceneStage {

    public StageAnswer(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        String toast = "Ok, answered this call.";
        speak(toast);
//                tts(toast, new TtsSpeaker.TtsStateChangedListener() {
//                    @Override
//                    public void onTtsStart() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsStart");
//                        }
//                    }
//
//                    @Override
//                    public void onTtsComplete() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsComplete");
//                        }
//                        answerPhoneCall();
//                    }
//
//                    @Override
//                    public void onTtsInterrupted() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsInterrupted");
//                        }
//                        answerPhoneCall();
//                    }
//
//                    @Override
//                    public void onTtsError() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsError");
//                        }
//                        answerPhoneCall();
//                    }
//
//                    private void answerPhoneCall() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.log(TAG, "answerPhoneCall");
//                        }
//                        TelephonyServiceManager.getIns().answerPhoneCall(KikaDialogFlowActivity.this);
//                        TelephonyServiceManager.getIns().turnOnSpeaker(KikaDialogFlowActivity.this);
//                    }
//                });

    }
}
