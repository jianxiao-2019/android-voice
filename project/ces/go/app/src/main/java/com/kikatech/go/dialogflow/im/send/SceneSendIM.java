package com.kikatech.go.dialogflow.im.send;

import android.content.Context;

import com.kikatech.go.dialogflow.im.IMContent;
import com.kikatech.go.dialogflow.im.send.stage.StageIdle;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class SceneSendIM extends SceneBase {

    public static final String SCENE = "Send IM";

    private IMContent mIMContent = null;

    public SceneSendIM(Context context, ISceneFeedback feedback) {
        super(context, feedback);
    }

    public IMContent getIMContent() {
        return mIMContent;
    }

    public void updateIMContent(IMContent ic) {
        if(mIMContent == null) {
            mIMContent = ic;
        } else {
            mIMContent.update(ic);
        }
    }

    @Override
    protected String scene() {
        return SCENE;
    }

    @Override
    protected void onExit() {
        mIMContent = null;
    }

    @Override
    protected SceneStage idle() {
        return new StageIdle(this, mFeedback);
    }
}
