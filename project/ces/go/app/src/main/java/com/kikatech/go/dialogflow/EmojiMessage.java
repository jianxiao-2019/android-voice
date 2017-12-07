package com.kikatech.go.dialogflow;

import android.text.TextUtils;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.util.EmojiUtil;

/**
 * Created by brad_chang on 2017/12/7.
 */

public class EmojiMessage {

    protected String messageBody = "";

    protected String emojiUnicode = "";
    protected String emojiDesc = "";
    protected boolean mSendWithEmoji = false;

    public void updateMsgBody(String msgBody) {
        if (LogUtil.DEBUG)
            LogUtil.log("EmojiMessage", "updateMsgBody:" + msgBody);
        this.messageBody = msgBody;
    }

    public String getMessageBody() {
        return getMessageBody(false);
    }

    public String getMessageBody(boolean checkEmoji) {
        if (checkEmoji && mSendWithEmoji) {
            return messageBody + emojiUnicode;
        } else {
            return messageBody;
        }
    }


    public void updateEmoji(String emojiJson) {
        EmojiUtil.EmojiInfo ei = EmojiUtil.parseEmojiJson(emojiJson);
        if (LogUtil.DEBUG)
            LogUtil.log("EmojiMessage", "updateEmoji:" + ei.unicode + " , " + ei.desc);
        emojiUnicode = ei.unicode;
        emojiDesc = ei.desc;
    }

    public String getEmojiUnicode() {
        return emojiUnicode;
    }

    public String getEmojiDesc() {
        return emojiDesc;
    }

    public boolean hasEmoji() {
        return !TextUtils.isEmpty(emojiUnicode) && !TextUtils.isEmpty(emojiDesc);
    }

    public void setSendWithEmoji(boolean b) {
        mSendWithEmoji = b;
    }
}
