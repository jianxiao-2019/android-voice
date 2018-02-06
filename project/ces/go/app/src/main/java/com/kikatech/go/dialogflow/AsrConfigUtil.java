package com.kikatech.go.dialogflow;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.kikatech.voice.service.conf.AsrConfiguration;

/**
 * Created by brad_chang on 2017/12/13.
 */

public class AsrConfigUtil {

    public static final int ASR_MODE_CONVERSATION_COMMAND   = 1;    // 長句指令
    public static final int ASR_MODE_CONVERSATION           = 2;    // 長句對話
    public static final int ASR_MODE_CONVERSATION_CMD_ALTER = 3;    // 長句指令可能糾錯
    public static final int ASR_MODE_CONVERSATION_ALTER     = 4;    // 長句對話可能糾錯
    public static final int ASR_MODE_SHORT_COMMAND          = 5;    // 短句指令 (不糾錯)
    public static final int ASR_MODE_SHORT_COMMAND_SPELLING = 6;    // 短句指令 (不糾錯, 需要拼寫)

    @IntDef({ASR_MODE_CONVERSATION_COMMAND, ASR_MODE_CONVERSATION, ASR_MODE_CONVERSATION_CMD_ALTER,
            ASR_MODE_CONVERSATION_ALTER, ASR_MODE_SHORT_COMMAND, ASR_MODE_SHORT_COMMAND_SPELLING})
    public @interface ASRMode {
        int ASR_MODE_DEFAULT = ASR_MODE_CONVERSATION_COMMAND;
    }


    static AsrConfiguration getConfig(@ASRMode int mode) {
        AsrConfiguration asrConf = new AsrConfiguration.Builder().build();
        return getConfig(asrConf, mode);
    }

    static String getAsrModeName(@ASRMode int mode) {
        switch (mode) {
            case ASR_MODE_CONVERSATION_COMMAND:
                return "CONVERSATION_COMMAND";
            case ASR_MODE_CONVERSATION:
                return "CONVERSATION";
            case ASR_MODE_CONVERSATION_CMD_ALTER:
                return "CONVERSATION_CMD_ALTER";
            case ASR_MODE_CONVERSATION_ALTER:
                return "CONVERSATION_ALTER";
            case ASR_MODE_SHORT_COMMAND:
                return "SHORT_COMMAND";
            case ASR_MODE_SHORT_COMMAND_SPELLING:
                return "SHORT_COMMAND_SPELLING";
        }
        return "ERR_UNKNOWN_ASR_MODE";
    }

    // https://docs.google.com/spreadsheets/d/1bKgyTxAGnNqjBviEZn_4_LfV9v-THbUg5F2xgggmMGk/edit#gid=0
    static AsrConfiguration getConfig(@NonNull AsrConfiguration asrConf, @ASRMode int mode) {
        switch (mode) {
            case ASR_MODE_CONVERSATION_COMMAND:
                asrConf.setAlterEnabled(false);
                asrConf.setEmojiEnabled(false);
                asrConf.setPunctuationEnabled(false);
                asrConf.setSpellingEnabled(false);
                asrConf.setEosPackets(3);
                break;
            case ASR_MODE_CONVERSATION:
                asrConf.setAlterEnabled(false);
                asrConf.setEmojiEnabled(true);
                asrConf.setPunctuationEnabled(true);
                asrConf.setSpellingEnabled(false);
                asrConf.setEosPackets(4);
                break;
            case ASR_MODE_CONVERSATION_CMD_ALTER:
                asrConf.setAlterEnabled(true);
                asrConf.setEmojiEnabled(false);
                asrConf.setPunctuationEnabled(false);
                asrConf.setSpellingEnabled(false);
                asrConf.setEosPackets(2);
                break;
            case ASR_MODE_CONVERSATION_ALTER:
                asrConf.setAlterEnabled(true);
                asrConf.setEmojiEnabled(true);
                asrConf.setPunctuationEnabled(true);
                asrConf.setSpellingEnabled(false);
                asrConf.setEosPackets(2);
                break;
            case ASR_MODE_SHORT_COMMAND:
                asrConf.setAlterEnabled(false);
                asrConf.setEmojiEnabled(false);
                asrConf.setPunctuationEnabled(false);
                asrConf.setSpellingEnabled(false);
                asrConf.setEosPackets(2);
                break;
            case ASR_MODE_SHORT_COMMAND_SPELLING:
                asrConf.setAlterEnabled(false);
                asrConf.setEmojiEnabled(false);
                asrConf.setPunctuationEnabled(false);
                asrConf.setSpellingEnabled(true);
                asrConf.setEosPackets(2);
                break;
        }

        // According to the CES demo request, disable all Emoji recommendation
        asrConf.setEmojiEnabled(false);

        return asrConf;
    }
}