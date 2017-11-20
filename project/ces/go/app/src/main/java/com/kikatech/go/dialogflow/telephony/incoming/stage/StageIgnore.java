package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.go.telephony.TelephonyServiceManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageIgnore extends SceneStage {
    private static final String TAG = "StageIgnore";

    public StageIgnore(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
        String speech = "Ok, ignore this call.";
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, speech);
        }
        speak(speech, new IDialogFlowFeedback.IToSceneFeedback() {
            @Override
            public void onTtsStart() {
            }

            @Override
            public void onTtsComplete() {
                ignorePhoneCall();
            }

            @Override
            public void onTtsError() {
                ignorePhoneCall();
            }

            @Override
            public void onTtsInterrupted() {
                ignorePhoneCall();
            }

            @Override
            public boolean isEndOfScene() {
                return true;
            }
        });
    }

    private void ignorePhoneCall() {
        TelephonyServiceManager.getIns().turnOnSilentMode(mSceneBase.getContext());
        exitScene();
    }
}
