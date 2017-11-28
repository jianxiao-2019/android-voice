package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSpeaker;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class AskToReadContentStage extends BaseStage {

    private final BaseIMObject mIMObject;

    AskToReadContentStage(@NonNull SceneBase scene, ISceneFeedback feedback, BaseIMObject imo) {
        super(scene, feedback);
        mIMObject = imo;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_IM_YES:
                return new ReadContentAndAskToReplyImStage(mSceneBase, mFeedback, mIMObject);
            case SceneActions.ACTION_REPLY_IM_NO:
            case SceneActions.ACTION_REPLY_IM_CANCEL:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Stop !!");
                exitScene();
                return null;
        }
        return this;
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    protected void action() {
        if(LogUtil.DEBUG) {
            LogUtil.log(TAG, "Receive message form " + mIMObject.getUserName() + ", do you want to play the message ?");
        }
        String[] uiAndTtsText = SceneUtil.getAskReadMsg(mSceneBase.getContext(), mIMObject.getUserName());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
            speak(ttsText, args);
        }
    }
}
