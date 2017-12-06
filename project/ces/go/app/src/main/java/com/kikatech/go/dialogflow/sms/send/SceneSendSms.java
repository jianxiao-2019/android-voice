package com.kikatech.go.dialogflow.sms.send;

import android.content.Context;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.send.stage.StageSendSmsIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.EmojiUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SceneSendSms extends NonLoopSceneBase {

    public static final String SCENE = "SendSMS";

    private SmsContent smsContent = null;

    public SceneSendSms(Context context, ISceneFeedback feedback) {
        super(context, feedback);
    }

    public SmsContent getSmsContent() {
        return smsContent;
    }

    public void updateSmsContent(SmsContent.IntentContent ic) {
        if(smsContent == null) {
            smsContent = new SmsContent(ic);
        } else {
            smsContent.update(ic);
        }
    }

    public void updateEmoji(String emojiJson) {
        EmojiUtil.EmojiInfo ei = EmojiUtil.parseEmojiJson(emojiJson);
        smsContent.updateEmoji(ei.unicode, ei.desc);
    }

    @Override
    protected String scene() {
        return SCENE;
    }

    @Override
    protected void onExit() {
        smsContent = null;
    }

    @Override
    protected SceneStage idle() {
        return new StageSendSmsIdle(this, mFeedback);
    }
}
