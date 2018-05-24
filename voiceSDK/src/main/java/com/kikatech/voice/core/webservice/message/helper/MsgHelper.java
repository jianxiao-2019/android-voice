package com.kikatech.voice.core.webservice.message.helper;

import android.text.TextUtils;

import com.kikatech.voice.core.webservice.message.AlterMessage;
import com.kikatech.voice.core.webservice.message.BosMessage;
import com.kikatech.voice.core.webservice.message.EmojiRecommendMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.NBestMessage;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.service.conf.VoiceConfiguration;

import java.util.HashMap;

/**
 * Created by ryanlin on 2018/5/24.
 */

public class MsgHelper {

    private final HashMap<String, Class<? extends Message>> TYPE_CLASS = new HashMap<>();

    public Message create(String type) {
        Class<?> clazz = TYPE_CLASS.get(type);
        if (clazz != null) {
            try {
                return (Message) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void registerMessage(VoiceConfiguration conf) {
        register(Message.MSG_TYPE_INTERMEDIATE, IntermediateMessage.class);
        register(Message.MSG_TYPE_ASR, TextMessage.class);
        register(Message.MSG_TYPE_BOS, BosMessage.class);

        register(Message.MSG_TYPE_ALTER, AlterMessage.class);

        AsrConfiguration asrConfiguration = conf.getConnectionConfiguration().getAsrConfiguration();
        if (asrConfiguration.getEmojiEnabled()) {
            register(Message.MSG_TYPE_EMOJI, EmojiRecommendMessage.class);
        }
        if (conf.getIsSupportNBest()) {
            register(Message.MSG_TYPE_NBEST, NBestMessage.class);
        }
    }

    private void register(String type, Class<? extends Message> clazz) {
        if (!TextUtils.isEmpty(type) && clazz != null) {
            TYPE_CLASS.put(type, clazz);
        }
    }

    public void unregisterMessage() {
        TYPE_CLASS.clear();
    }

}