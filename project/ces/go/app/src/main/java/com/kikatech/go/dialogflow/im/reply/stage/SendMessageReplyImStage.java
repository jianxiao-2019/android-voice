package com.kikatech.go.dialogflow.im.reply.stage;

import android.support.annotation.NonNull;

import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.message.im.IMManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class SendMessageReplyImStage extends BaseStage {

    private final String mMsgBody;
    private final BaseIMObject mIMObject;

    SendMessageReplyImStage(@NonNull SceneBase scene, ISceneFeedback feedback, @NonNull BaseIMObject imo, @NonNull String messageBody) {
        super(scene, feedback);
        mIMObject = imo;
        mMsgBody = messageBody;
    }

    @Override
    public void action() {
        boolean sent = IMManager.getInstance().sendMessage(mSceneBase.getContext(), mIMObject, mMsgBody);
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "Send Message : \n" + mMsgBody + ", target:" + mIMObject + ", sent:" + sent);

        exitScene();
    }
}
