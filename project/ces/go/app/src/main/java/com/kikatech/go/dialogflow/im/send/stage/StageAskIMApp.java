package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageAskIMApp extends BaseSendIMStage {

    private final boolean mIMNotSupported;
    private String usedSaidImApp = "";

    /**
     * 6.1 詢問使用何者 IM
     */
    StageAskIMApp(@NonNull SceneBase scene, ISceneFeedback feedback, boolean IMNotSupported) {
        super(scene, feedback);
        mIMNotSupported = IMNotSupported;
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        //onIntent : Send IM - input.unknown : Bundle[{custom_intent_key_user_input=what's up}]
        if (action.equals(Intent.ACTION_UNKNOWN)) {
            usedSaidImApp = Intent.parseUserInput(extra);
        }
        return getCheckIMAppStage(TAG, getIMContent(), mSceneBase, mFeedback);
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "mIMNotSupported:" + mIMNotSupported);
        String[] uiAndTtsText;
        if (!TextUtils.isEmpty(usedSaidImApp)) {
            // TODO
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "User wants to use '" + usedSaidImApp + "', but we don't support ... ");
            uiAndTtsText = SceneUtil.getAskApp(mSceneBase.getContext());
            uiAndTtsText[1] = "I cannot get what you said, " + uiAndTtsText[1];
        } else if (mIMNotSupported) {
            // TODO: should we tell users that app is not installed or not supported, instead of asking again?
            uiAndTtsText = SceneUtil.getAskApp(mSceneBase.getContext());
            uiAndTtsText[1] = getIMContent().getIMAppPackageName() + " is not available, " + uiAndTtsText[1];
        } else {
            uiAndTtsText = SceneUtil.getAskApp(mSceneBase.getContext());
        }
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
            speak(ttsText, args);
        }
    }
}