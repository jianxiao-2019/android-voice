package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.message.im.IMManager;
import com.kikatech.go.util.BackgroundThread;
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
    public void doAction() {
        action();
    }

    @Override
    public void action() {
        Bundle args = new Bundle();
        args.putString(SceneUtil.EXTRA_EVENT, SceneUtil.EVENT_DISPLAY_MSG_SENT);
        args.putInt(SceneUtil.EXTRA_ALERT, R.raw.alert_succeed);
        send(args);
        boolean sent = IMManager.getInstance().sendMessage(mSceneBase.getContext(), mIMObject, mMsgBody);
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Send Message : \n" + mMsgBody + ", target:" + mIMObject + ", sent:" + sent);
        }
        BackgroundThread.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exitScene();
            }
        }, 1500);
    }
}
