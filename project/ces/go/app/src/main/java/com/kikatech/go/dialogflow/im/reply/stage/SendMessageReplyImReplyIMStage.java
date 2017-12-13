package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.reply.ReplyIMMessage;
import com.kikatech.go.message.im.IMManager;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class SendMessageReplyImReplyIMStage extends BaseReplyIMStage {

    SendMessageReplyImReplyIMStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
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
        ReplyIMMessage rimm = getReplyMessage();
        boolean sent = IMManager.getInstance().sendMessage(mSceneBase.getContext(), rimm.getIMObject(), rimm.getMessageBody(true));
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Send Message : \n" + rimm.getMessageBody() + ", target:" + rimm.getIMObject() + ", sent:" + sent);
        }
        BackgroundThread.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exitScene();
            }
        }, SceneUtil.MSG_SENT_PAGE_DELAY);
    }
}
