package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.navigation.google.webservice.GooglePlaceApi;
import com.kikatech.go.navigation.model.Place;
import com.kikatech.go.navigation.model.PlaceSearchResult;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/12/8.
 */

public class StageQueryAddress extends BaseNaviStage {

    private String mUserInput;
    private PlaceSearchResult mPlaceSearchResult;

    StageQueryAddress(@NonNull SceneBase scene, ISceneFeedback feedback, String userInput) {
        super(scene, feedback);
        mUserInput = userInput;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        if (!TextUtils.isEmpty(mUserInput)) {
            GooglePlaceApi.getIns().search(mUserInput, new GooglePlaceApi.IOnSearchResultListener() {
                @Override
                public void onResult(PlaceSearchResult result) {
                    mPlaceSearchResult = result;
                    onStageActionDone(false, false);
                }

                @Override
                public void onError(String err) {
                    if (LogUtil.DEBUG) {
                        LogUtil.logw(TAG, String.format("onError: %s", err));
                    }
                    onStageActionDone(false, false);
                }
            });
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
        SceneStage nextStage = new StageAskAddress(mSceneBase, mFeedback, true);
        if (mPlaceSearchResult != null) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onResult");
                mPlaceSearchResult.print();
            }
            Place place = mPlaceSearchResult.get(0);
            if (place != null) {
                nextStage = new StageConfirmAddress(mSceneBase, mFeedback, mUserInput, place.getFormattedAddress());
            }
        }
        mSceneBase.nextStage(nextStage);
    }
}
