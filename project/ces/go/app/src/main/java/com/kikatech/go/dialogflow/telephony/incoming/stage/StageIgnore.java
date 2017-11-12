package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.go.telephony.TelephonyServiceManager;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageIgnore extends SceneStage {

    public StageIgnore(ISceneFeedback feedback) {
        super(feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        speak("Ok, ignore this call.");
//        toast = "Ok, ignore this call.";
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
//                        ignorePhoneCall();
//                    }
//
//                    @Override
//                    public void onTtsInterrupted() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsInterrupted");
//                        }
//                        ignorePhoneCall();
//                    }
//
//                    @Override
//                    public void onTtsError() {
//                        if (LogUtil.DEBUG) {
//                            LogUtil.logv(TAG, "onTtsError");
//                        }
//                        ignorePhoneCall();
//                    }
//                });
    }

    private void ignorePhoneCall() {
//        if (LogUtil.DEBUG) {
//            LogUtil.log(TAG, "ignorePhoneCall");
//        }
//        TelephonyServiceManager.getIns().turnOnSilentMode(KikaDialogFlowActivity.this);
    }
}
