package com.kikatech.go.dialogflow.help.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.util.StringUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2018/5/2.
 */

class StageHelpStart extends BaseHelpStage {

    StageHelpStart(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    protected void action() {
        OptionList defaultList = OptionList.getDefaultOptionList();
        List<String> displayTexts = new ArrayList<>();
        for (Option option : defaultList.getList()) {
            displayTexts.add(option.getDisplayText());
        }
        String allOptions = StringUtil.join(displayTexts, " or ");
        String ttsText = SceneUtil.getHelp(mSceneBase.getContext(), allOptions);
        Bundle args = new Bundle();
        args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, defaultList);
        speak(ttsText, args);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
        super.onStageActionDone(isInterrupted, delayAsrResume);
    }
}
