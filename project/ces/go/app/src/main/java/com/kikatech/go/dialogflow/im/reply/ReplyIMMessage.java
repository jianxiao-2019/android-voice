package com.kikatech.go.dialogflow.im.reply;

import com.kikatech.go.dialogflow.EmojiMessage;
import com.kikatech.go.message.im.BaseIMObject;

/**
 * Created by brad_chang on 2017/12/7.
 */

public class ReplyIMMessage extends EmojiMessage {

    private BaseIMObject imo = null;

    void updateIMObject(BaseIMObject imo) {
        this.imo = imo;
    }

    public BaseIMObject getIMObject() {
        return imo;
    }
}
