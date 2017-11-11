package com.kikatech.voice.dialogflow.telephony.incoming;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class SceneIncoming extends SceneBase {

    public static final String SCENE = "Telephony - Incoming";

    @Override
    protected SceneStage init() {
        return new SceneStage() {
            @Override
            public SceneStage next(String action, Bundle extra) {
                if (SceneActions.ACTION_TELEPHONY_INCOMING_START.equals(action)) {
//                    return new com.kikatech.voice.dialogflow.telephony.incoming.stage.StageIncoming();
                }
                return null;
            }

            @Override
            public void action() {
            }
        };
    }
}
