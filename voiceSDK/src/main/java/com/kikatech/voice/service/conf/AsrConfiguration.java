package com.kikatech.voice.service.conf;

import com.google.gson.Gson;

/**
 * Created by ryanlin on 12/12/2017.
 */

public class AsrConfiguration {

    public enum SpeechMode {
        ONE_SHOT,
        CONVERSATION
    }

    private SpeechMode speechMode;
    private boolean spellingEnabled;
    private boolean alterEnabled;
    private boolean emojiEnabled;
    private boolean punctuationEnabled;

    private AsrConfiguration(SpeechMode speechMode, boolean spellingEnabled, boolean alterEnabled,
                             boolean emojiEnable, boolean punctuationEnabled) {
        setConfig(speechMode, spellingEnabled, alterEnabled, emojiEnable, punctuationEnabled);
    }

    public void copyConfig(AsrConfiguration conf) {
        setConfig(conf.speechMode, conf.spellingEnabled, conf.alterEnabled, conf.emojiEnabled, conf.punctuationEnabled);
    }

    private void setConfig(SpeechMode speechMode, boolean spellingEnabled, boolean alterEnabled,
                            boolean emojiEnable, boolean punctuationEnabled) {
        this.spellingEnabled = spellingEnabled;
        this.speechMode = speechMode;
        this.alterEnabled = alterEnabled;
        this.emojiEnabled = emojiEnable;
        this.punctuationEnabled = punctuationEnabled;
    }

    public void setSpeechMode(SpeechMode speechMode) {
        this.speechMode = speechMode;
    }

    public SpeechMode getSpeechMode() {
        return this.speechMode;
    }

    public void setSpellingEnabled(boolean spellingEnabled) {
        this.spellingEnabled = spellingEnabled;
    }

    public boolean getSpellingEnabled() {
        return this.spellingEnabled;
    }

    public void setAlterEnabled(boolean alterEnabled) {
        this.alterEnabled = alterEnabled;
    }

    public boolean getAlterEnabled() {
        return this.alterEnabled;
    }

    public void setEmojiEnabled(boolean emojiEnabled) {
        this.emojiEnabled = emojiEnabled;
    }

    public boolean getEmojiEnabled() {
        return this.emojiEnabled;
    }

    public void setPunctuationEnabled(boolean punctuationEnabled) {
        this.punctuationEnabled = punctuationEnabled;
    }

    public boolean getPunctuationEnabled() {
        return this.punctuationEnabled;
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public boolean update(AsrConfiguration asrConfig) {
        if (sameValue(asrConfig)) {
            return false;
        } else {
            copyConfig(asrConfig);
            return true;
        }
    }

    private boolean sameValue(AsrConfiguration asrConfig) {
        return speechMode == asrConfig.speechMode &&
                alterEnabled == asrConfig.alterEnabled &&
                emojiEnabled == asrConfig.emojiEnabled &&
                punctuationEnabled == asrConfig.punctuationEnabled;
    }

    public static class Builder {
        private SpeechMode speechMode = SpeechMode.CONVERSATION;
        private boolean spellingEnabled = false;
        private boolean alterEnabled = false;
        private boolean emojiEnabled = false;
        private boolean punctuationEnabled = false;

        public Builder setSpeechMode(SpeechMode speechMode) {
            this.speechMode = speechMode;
            return this;
        }

        public Builder setSpellingEnabled(boolean spellingEnabled) {
            this.spellingEnabled = spellingEnabled;
            return this;
        }

        public Builder setAlterEnabled(boolean alterEnabled) {
            this.alterEnabled = alterEnabled;
            return this;
        }

        public Builder setEmojiEnabled(boolean emojiEnabled) {
            this.emojiEnabled = emojiEnabled;
            return this;
        }

        public Builder setPunctuationEnabled(boolean punctuationEnabled) {
            this.punctuationEnabled = punctuationEnabled;
            return this;
        }

        public AsrConfiguration build() {
            return new AsrConfiguration(speechMode, spellingEnabled, alterEnabled, emojiEnabled, punctuationEnabled);
        }
    }
}