package com.kikatech.go.dialogflow.sms.send.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import java.util.List;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskForChooseNumbers extends BaseSendSmsStage {

    private final static byte ERR_STATUS_NONE = 0;
    private final static byte ERR_STATUS_ACTION_NOT_SUPPORTED = 1;
    private final static byte ERR_STATUS_OPTION_IS_EMPTY = 2;
    private final static byte ERR_STATUS_OPTION_OOI = 3;
    private final static byte ERR_STATUS_SAY_AGAIN = 4;

    private final byte mErrStatus;

    /**
     * SendSMS 2.6 向用戶進一步確認號碼或識別標籤
     */
    StageAskForChooseNumbers(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        mErrStatus = ERR_STATUS_NONE;
    }

    private StageAskForChooseNumbers(@NonNull SceneBase scene, ISceneFeedback feedback, byte err) {
        super(scene, feedback);
        mErrStatus = err;
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        if (action.equals(SceneActions.ACTION_SEND_SMS_AGAIN)) {
            return new StageAskForChooseNumbers(mSceneBase, mFeedback, ERR_STATUS_SAY_AGAIN);
        }

        if (!action.equals(SceneActions.ACTION_SEND_SMS_SELECT_NUM)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
            return new StageAskForChooseNumbers(mSceneBase, mFeedback, ERR_STATUS_ACTION_NOT_SUPPORTED);
        }

        SmsContent sc = getSmsContent();
        int chosenOption = sc.getChosenOption();
        if (chosenOption == -1) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Error, chosenOption is empty , Fallback !!");
            return new StageAskForChooseNumbers(mSceneBase, mFeedback, ERR_STATUS_OPTION_IS_EMPTY);
        } else {
            final int chosenIdx = chosenOption - 1;
            if (chosenIdx < 0 || chosenIdx >= sc.getNumberCount()) {
                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "Error, option is out og index, chosenIdx=" + chosenIdx + ", phone count=" + sc.getNumberCount());
                return new StageAskForChooseNumbers(mSceneBase, mFeedback, ERR_STATUS_OPTION_OOI);
            }

            sc.setChosenNumber(sc.getPhoneNumbers().get(chosenIdx));
            return getStageCheckSmsBody(TAG, sc, mSceneBase, mFeedback);
        }
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG && mErrStatus != ERR_STATUS_NONE) {
            LogUtil.log(TAG, "Error Status : " + mErrStatus);
        }
        List<String> numbers = getSmsContent().getPhoneNumbers();
        if (numbers.size() > 1) {
            Context context = mSceneBase.getContext();
            String[] uiAndTtsText = SceneUtil.getOptionListCommon(context);
            if (uiAndTtsText.length > 0) {
                Bundle args = new Bundle();
                String uiText = uiAndTtsText[0];
                String ttsText = uiAndTtsText[1];
                OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_ORDINAL);
                optionList.setTitle(uiText);
                int iteratorSize = numbers.size() > 2 ? 2 : numbers.size();
                for (int i = 0; i < iteratorSize; i++) {
                    String number = numbers.get(i);
                    optionList.add(new Option(number, null));
                }
                args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
                String speech = optionList.getTextToSpeak(ttsText);
                speak(speech, args);
            }
        } else {
            speak("Error, only one phone number");
        }
    }
}