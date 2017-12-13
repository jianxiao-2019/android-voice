package com.kikatech.go.dialogflow;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.kikatech.voice.service.conf.AsrConfiguration;

/**
 * Created by brad_chang on 2017/12/13.
 */

public class AsrConfigUtil {

    public static final int SUGGEST_ASR_MODE_CONVERSATION_NORMAL = 1;
    public static final int SUGGEST_ASR_MODE_CONVERSATION_PLUS = 2;
    public static final int SUGGEST_ASR_MODE_COMMAND_ALTER = 3;
    public static final int SUGGEST_ASR_MODE_COMMAND_FULL = 4;
    public static final int SUGGEST_ASR_MODE_COMMAND_ONLY = 5;

    public static final int SUGGEST_ASR_MODE_DEFAULT = SUGGEST_ASR_MODE_CONVERSATION_NORMAL;

    @IntDef({SUGGEST_ASR_MODE_CONVERSATION_NORMAL, SUGGEST_ASR_MODE_CONVERSATION_PLUS,
            SUGGEST_ASR_MODE_COMMAND_ALTER, SUGGEST_ASR_MODE_COMMAND_FULL, SUGGEST_ASR_MODE_COMMAND_ONLY})
    public @interface ASRMode {
    }


    static AsrConfiguration getConfig(@ASRMode int mode) {
        AsrConfiguration asrConf = new AsrConfiguration.Builder().build();
        return getConfig(asrConf, mode);
    }

    static String getAsrModeName(@ASRMode int mode) {
        switch (mode) {
            case SUGGEST_ASR_MODE_CONVERSATION_NORMAL:
                return "CONVERSATION_NORMAL";
            case SUGGEST_ASR_MODE_CONVERSATION_PLUS:
                return "CONVERSATION_PLUS";
            case SUGGEST_ASR_MODE_COMMAND_ALTER:
                return "COMMAND_ALTER";
            case SUGGEST_ASR_MODE_COMMAND_FULL:
                return "COMMAND_FULL";
            case SUGGEST_ASR_MODE_COMMAND_ONLY:
                return "COMMAND_ONLY";
        }
        return "ERR_UNKNOWN_MODE";
    }

    // https://docs.google.com/spreadsheets/d/1bKgyTxAGnNqjBviEZn_4_LfV9v-THbUg5F2xgggmMGk/edit#gid=0
    static AsrConfiguration getConfig(@NonNull AsrConfiguration asrConf, @ASRMode int mode) {
        switch (mode) {
            case SUGGEST_ASR_MODE_CONVERSATION_NORMAL:
                asrConf.setSpeechMode(AsrConfiguration.SpeechMode.CONVERSATION);
                asrConf.setAlterEnabled(false);
                asrConf.setEmojiEnabled(false);
                asrConf.setPunctuationEnabled(false);
                break;
            case SUGGEST_ASR_MODE_CONVERSATION_PLUS:
                asrConf.setSpeechMode(AsrConfiguration.SpeechMode.CONVERSATION);
                asrConf.setAlterEnabled(false);
                asrConf.setEmojiEnabled(true);
                asrConf.setPunctuationEnabled(true);
                break;
            case SUGGEST_ASR_MODE_COMMAND_ALTER:
                asrConf.setSpeechMode(AsrConfiguration.SpeechMode.ONE_SHOT);
                asrConf.setAlterEnabled(true);
                asrConf.setEmojiEnabled(false);
                asrConf.setPunctuationEnabled(false);
                break;
            case SUGGEST_ASR_MODE_COMMAND_FULL:
                asrConf.setSpeechMode(AsrConfiguration.SpeechMode.ONE_SHOT);
                asrConf.setAlterEnabled(true);
                asrConf.setEmojiEnabled(true);
                asrConf.setPunctuationEnabled(true);
                break;
            case SUGGEST_ASR_MODE_COMMAND_ONLY:
                asrConf.setSpeechMode(AsrConfiguration.SpeechMode.ONE_SHOT);
                asrConf.setAlterEnabled(false);
                asrConf.setEmojiEnabled(false);
                asrConf.setPunctuationEnabled(false);
                break;
        }
        return asrConf;
    }
}