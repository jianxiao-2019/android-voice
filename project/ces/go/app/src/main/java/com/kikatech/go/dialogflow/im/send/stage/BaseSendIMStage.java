package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.im.send.IMContent;
import com.kikatech.go.dialogflow.im.IMUtil;
import com.kikatech.go.dialogflow.im.send.SceneSendIM;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class BaseSendIMStage extends BaseSceneStage {

    BaseSendIMStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) LogUtil.log(TAG, "init");
    }

    IMContent getIMContent() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, ((SceneSendIM) mSceneBase).getIMContent().toString());
        }
        return ((SceneSendIM) mSceneBase).getIMContent();
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        // Parse IM content
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action : " + action);
        if (action.equals(Intent.ACTION_RCMD_EMOJI)) {
            String emojiJson = Intent.parseEmojiJsonString(extra);
            ((SceneSendIM) mSceneBase).updateEmoji(emojiJson);
        } else {
            IMContent imc = IMUtil.parse(extra);
            ((SceneSendIM) mSceneBase).updateIMContent(imc);
        }
        return getNextStage(action, extra);
    }

    protected SceneStage getNextStage(String action, Bundle extra) {
        return null;
    }

    @Override
    public void prepare() {
        //if (LogUtil.DEBUG) LogUtil.log(TAG, "prepare : do nothing");
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action : do nothing");
    }

    static SceneStage getCheckIMBodyStage(String TAG, IMContent imc, SceneBase scene, ISceneFeedback feedback) {
        String msgBody = imc.getMessageBody();

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "getCheckIMBodyStage, msgBody:" + msgBody);
        }

        if(TextUtils.isEmpty(msgBody)) {
            return new StageAskMsgBody(scene, feedback);
        } else {
            return new StageConfirmSendMessage(scene, feedback);
        }
    }

    // 6.2
    static SceneStage getCheckSendTargetStage(String TAG, IMContent imc, SceneBase scene, ISceneFeedback feedback) {
        String sendTarget = imc.getSendTarget();

        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "getCheckSendTargetStage, sendTarget:" + sendTarget);
        }

        if (TextUtils.isEmpty(sendTarget)) {
            return new StageAskSendTarget(scene, feedback);
        } else {
            if (imc.isExplicitTarget(scene.getContext())) {
                return getCheckIMBodyStage(TAG, imc, scene, feedback);
            } else {
                return new StageConfirmSendTarget(scene, feedback);
            }
        }
    }

    // 6.1
    static SceneStage getCheckIMAppStage(String TAG, IMContent imc, SceneBase scene, ISceneFeedback feedback) {
        if(LogUtil.DEBUG) {
            LogUtil.log(TAG, "getCheckIMAppStage");
        }
        String imAppPackageName = imc.getIMAppPackageName();
        if(TextUtils.isEmpty(imAppPackageName)) {
            // 6.1 詢問使用何者 IM
            return new StageAskIMApp(scene, feedback, false);
        } else {
            if (IMUtil.isIMAppSupported(scene.getContext(), imAppPackageName)) {
                return getCheckSendTargetStage(TAG, imc, scene, feedback);
            } else {
                // TODO check im app
                if(LogUtil.DEBUG) {
                    LogUtil.log(TAG, "Err, " + imAppPackageName + " is not supported !!");
                }
                return new StageAskIMApp(scene, feedback, true);
            }
        }
    }
}