package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.app.Activity;
import android.os.Bundle;

import com.kikatech.go.telephony.TelephonyServiceManager;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageReject extends SceneStage {

    public StageReject(ISceneFeedback feedback) {
        super(feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        String toast = "Ok, rejected this call.";
        toast = "Ok, rejected this call.";
//                tts(toast, new TtsSpeaker.TtsStateChangedListener() {
//                    @Override
//                    public void onTtsStart() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsStart");
//                        }
//                    }
//                    @Override
//                    public void onTtsComplete() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsComplete");
//                        }
//                        rejectPhoneCall();
//                    }
//
//                    @Override
//                    public void onTtsInterrupted() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsInterrupted");
//                        }
//                        rejectPhoneCall();
//                    }
//
//                    @Override
//                    public void onTtsError() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsError");
//                        }
//                        rejectPhoneCall();
//                    }
//
//                });
        speak(toast);
    }

    private void rejectPhoneCall() {
//        TelephonyServiceManager.getIns().killPhoneCall();
    }

}
