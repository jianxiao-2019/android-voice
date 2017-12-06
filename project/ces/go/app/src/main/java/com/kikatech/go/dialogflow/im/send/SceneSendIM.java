package com.kikatech.go.dialogflow.im.send;

import android.content.Context;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.im.IMContent;
import com.kikatech.go.dialogflow.im.IMUtil;
import com.kikatech.go.dialogflow.im.send.stage.StageIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.EmojiUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class SceneSendIM extends NonLoopSceneBase {

    public static final String SCENE = "Send IM";

    private IMContent mIMContent = null;

    public SceneSendIM(Context context, ISceneFeedback feedback) {
        super(context, feedback);
    }

    public IMContent getIMContent() {
        return mIMContent;
    }

    public void updateIMContent(IMContent ic) {
        if(mIMContent == null) {
            mIMContent = ic;
        } else {
            mIMContent.update(ic);
        }
    }

    public void updateEmoji(String emojiJson) {
        EmojiUtil.EmojiInfo ei = EmojiUtil.parseEmojiJson(emojiJson);
        mIMContent.updateEmoji(ei.unicode, ei.desc);
    }

    @Override
    protected String scene() {
        return SCENE;
    }

    @Override
    protected void onExit() {
        mIMContent = null;
    }

    @Override
    protected SceneStage idle() {
        return new StageIdle(this, mFeedback);
    }

    @Override
    protected String getTransformSceneInfo() {
        return IMUtil.prepareSwitchSceneInfo(mIMContent);
    }

    @Override
    protected boolean supportEmoji() {
        return true;
    }
}
