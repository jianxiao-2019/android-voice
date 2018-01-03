package com.kikatech.go.dialogflow.ces.demo.wakeup.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/2.
 */

public class StageWakeUpFunny extends BaseSceneStage {
    private static final String TAG = "StageWakeUpFunny";

    public StageWakeUpFunny(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "constructor");
        }
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, String.format("action: %s", action));
        }
        return null;
    }

    @Override
    public void doAction() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "doAction");
        }
        onStageActionStart();
        action();
    }

    @Override
    protected void prepare() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "prepare");
        }
    }

    @Override
    protected void action() {
//        Context context = mSceneBase.getContext();
//        String[] uiAndTtsText = SceneUtil.getAskEmoji(context, getSmsContent().getEmojiUnicode());
        final String tmp = "Hey, sis! Someone is calling you. Time to wake up!";
        String[] uiAndTtsText = new String[]{tmp, tmp};
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_MSG, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
        exitScene();
    }
}