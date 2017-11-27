package com.kikatech.go.dialogflow.navigation.stage;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.navigation.NaviSceneActions;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageConfirmAddress extends BaseNaviStage {

    private final String mNaviAddress;

    StageConfirmAddress(SceneBase scene, ISceneFeedback feedback, String naviAddress) {
        super(scene, feedback);
        mNaviAddress = naviAddress;
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "StageConfirmAddress init, mNaviAddress:" + mNaviAddress);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        super.next(action, extra);
        if (mStopNavi) {
            return null;
        }

        switch (action) {
            case NaviSceneActions.ACTION_NAV_YES:
                return new StageNavigationGo(mSceneBase, mFeedback, mNaviAddress);
            case NaviSceneActions.ACTION_NAV_NO:
                return new StageAskAddress(mSceneBase, mFeedback, false);
            case NaviSceneActions.ACTION_NAV_CHANGE:
                String naviAddress = NaviSceneUtil.parseAddress(extra);
                if (LogUtil.DEBUG) LogUtil.log(TAG, "naviAddress:" + naviAddress);
                return new StageConfirmAddress(mSceneBase, mFeedback, naviAddress);
        }
        return this;
    }

    @Override
    public void action() {
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmAddress(context, mNaviAddress);
        if (uiAndTtsText.length > 0) {
            String[] options = SceneUtil.getOptionsCommon(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
            optionList.setTitle(uiText);
            for (String option : options) {
                optionList.add(new Option(option, null));
            }
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
            speak(ttsText, args);
        }
    }
}
